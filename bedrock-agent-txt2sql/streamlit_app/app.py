from flask import Flask, request, jsonify
from flask_cors import CORS
from boto3.session import Session
from botocore.auth import SigV4Auth
from botocore.awsrequest import AWSRequest
from botocore.credentials import Credentials
import json
import os
from requests import request
import base64
import io
import sys

app = Flask(__name__)
CORS(app)

agentId = "<YOUR AGENT ID>"
agentAliasId = "<YOUR ALIAS ID>"
theRegion = "us-west-2"
os.environ["AWS_REGION"] = theRegion
region = os.environ.get("AWS_REGION")
llm_response = ""

def sigv4_request(
    url,
    method='GET',
    body=None,
    params=None,
    headers=None,
    service='execute-api',
    region=os.environ['AWS_REGION'],
    credentials=Session().get_credentials().get_frozen_credentials()
):
    req = AWSRequest(
        method=method,
        url=url,
        data=body,
        params=params,
        headers=headers
    )
    SigV4Auth(credentials, service, region).add_auth(req)
    req = req.prepare()

    return request(
        method=req.method,
        url=req.url,
        headers=req.headers,
        data=req.body
    )

def askQuestion(question, url, endSession=False):
    myobj = {
        "inputText": question,   
        "enableTrace": True,
        "endSession": endSession
    }
    
    response = sigv4_request(
        url,
        method='POST',
        service='bedrock',
        headers={
            'content-type': 'application/json', 
            'accept': 'application/json',
        },
        region=theRegion,
        body=json.dumps(myobj)
    )
    
    return decode_response(response)

def decode_response(response):
    captured_output = io.StringIO()
    sys.stdout = captured_output

    string = ""
    for line in response.iter_content():
        try:
            string += line.decode(encoding='utf-8')
        except:
            continue

    print("Decoded response", string)
    split_response = string.split(":message-type")
    print(f"Split Response: {split_response}")
    print(f"length of split: {len(split_response)}")

    for idx in range(len(split_response)):
        if "bytes" in split_response[idx]:
            encoded_last_response = split_response[idx].split("\"")[3]
            decoded = base64.b64decode(encoded_last_response)
            final_response = decoded.decode('utf-8')
            print(final_response)
        else:
            print(f"no bytes at index {idx}")
            print(split_response[idx])
            
    last_response = split_response[-1]
    print(f"Lst Response: {last_response}")
    if "bytes" in last_response:
        print("Bytes in last response")
        encoded_last_response = last_response.split("\"")[3]
        decoded = base64.b64decode(encoded_last_response)
        final_response = decoded.decode('utf-8')
    else:
        print("no bytes in last response")
        part1 = string[string.find('finalResponse')+len('finalResponse":'):] 
        part2 = part1[:part1.find('"}')+2]
        final_response = json.loads(part2)['text']

    final_response = final_response.replace("\"", "")
    final_response = final_response.replace("{input:{value:", "")
    final_response = final_response.replace(",source:null}}", "")
    llm_response = final_response

    sys.stdout = sys.__stdout__

    captured_string = captured_output.getvalue()

    return captured_string, llm_response

@app.route('/invoke', methods=['POST'])
def invoke():
    data = request.json
    question = data.get('question')
    session_id = data.get('sessionId', 'MYSESSION')
    end_session = data.get('endSession', False)
    
    url = f'https://bedrock-agent-runtime.{theRegion}.amazonaws.com/agents/{agentId}/agentAliases/{agentAliasId}/sessions/{session_id}/text'

    try:
        response, trace_data = askQuestion(question, url, end_session)
        return jsonify({
            "response": response,
            "trace_data": trace_data
        }), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)