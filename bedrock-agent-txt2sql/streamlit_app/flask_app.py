app = Flask(__name__)
import InvokeAgent as agenthelper
from flask import Flask, request, jsonify
import json
import pandas as pd
from PIL import Image, ImageOps, ImageDraw

app.set_page_config(page_title="Text2SQL Agent", page_icon=":robot_face:", layout="wide")

# Function to crop image into a circle
def crop_to_circle(image):
    mask = Image.new('L', image.size, 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.ellipse((0, 0) + image.size, fill=255)
    result = ImageOps.fit(image, mask.size, centering=(0.5, 0.5))
    result.putalpha(mask)
    return result

# Title
app.title("Text2SQL Agent - Amazon Athena")

# Display a text box for input
prompt = app.text_input("Please enter your query?", max_chars=2000)
prompt = prompt.strip()

# Display a primary button for submission
submit_button = app.button("Submit", type="primary")

# Display a button to end the session
end_session_button = app.button("End Session")

# Sidebar for user input
app.sidebar.title("Trace Data")




# Session State Management
if 'history' not in app.session_state:
    app.session_state['history'] = []

# Function to parse and format response
def format_response(response_body):
    try:
        # Try to load the response as JSON
        data = json.loads(response_body)
        # If it's a list, convert it to a DataFrame for better visualization
        if isinstance(data, list):
            return pd.DataFrame(data)
        else:
            return response_body
    except json.JSONDecodeError:
        # If response is not JSON, return as is
        return response_body



# Handling user input and responses
if submit_button and prompt:
    event = {
        "sessionId": "MYSESSION",
        "question": prompt
    }
    response = agenthelper.lambda_handler(event, None)
    
    try:
        # Parse the JSON string
        if response and 'body' in response and response['body']:
            response_data = json.loads(response['body'])
            print("TRACE & RESPONSE DATA ->  ", response_data)
        else:
            print("Invalid or empty response received")
    except json.JSONDecodeError as e:
        print("JSON decoding error:", e)
        response_data = None 
    
    try:
        # Extract the response and trace data
        all_data = format_response(response_data['response'])
        the_response = response_data['trace_data']
    except:
        all_data = "..." 
        the_response = "Apologies, but an error occurred. Please rerun the application" 

    # Use trace_data and formatted_response as needed
    app.sidebar.text_area("", value=all_data, height=300)
    app.session_state['history'].append({"question": prompt, "answer": the_response})
    app.session_state['trace_data'] = the_response

    
    

if end_session_button:
    app.session_state['history'].append({"question": "Session Ended", "answer": "Thank you for using AnyCompany Support Agent!"})
    event = {
        "sessionId": "MYSESSION",
        "question": "placeholder to end session",
        "endSession": True
    }
    agenthelper.lambda_handler(event, None)
    app.session_state['history'].clear()


# Display conversation history
app.write("## Conversation History")

for chat in reversed(app.session_state['history']):
    
    # Creating columns for Question
    col1_q, col2_q = app.columns([2, 10])
    with col1_q:
        human_image = Image.open('images/human_face.png')
        circular_human_image = crop_to_circle(human_image)
        app.image(circular_human_image, width=125)
    with col2_q:
        app.text_area("Q:", value=chat["question"], height=50, key=str(chat)+"q", disabled=True)

    # Creating columns for Answer
    col1_a, col2_a = app.columns([2, 10])
    if isinstance(chat["answer"], pd.DataFrame):
        with col1_a:
            robot_image = Image.open('images/robot_face.jpg')
            circular_robot_image = crop_to_circle(robot_image)
            app.image(circular_robot_image, width=100)
        with col2_a:
            app.dataframe(chat["answer"])
    else:
        with col1_a:
            robot_image = Image.open('images/robot_face.jpg')
            circular_robot_image = crop_to_circle(robot_image)
            app.image(circular_robot_image, width=150)
        with col2_a:
            app.text_area("A:", value=chat["answer"], height=100, key=str(chat)+"a")


# Example Prompts Section


# Increase the maximum width of the text in each cell of the dataframe
pd.set_option('display.max_colwidth', None)

# Define the queries and their descriptions
query_data = {
    "Test Prompts": [
        "Show me all procedures in the imaging category that are insured.",
        "Return to me the number of procedures that are in the laboratory category.",
        "Let me see the number of procedures that are either in the laboratory, imaging, or surgery category, and insured.",
        "Return me information on all customers who have a past due amount over 70.",
        "Provide me details on all customers who are VIP, and have a balance over 300.",
        "Get me data of all procedures that were not insured, with customer names."
    ]
}

# Create DataFrame
queries_df = pd.DataFrame(query_data)

# Display the DataFrame in Streamlit
app.write("## Test Prompts for Amazon Athena")
app.dataframe(queries_df, width=900)  # Adjust the width to fit your layout
n@app.route("/invoke", methods=["POST"])
def invoke_endpoint():
    data = request.json
    response = invoke(data)
    return jsonify(response)

if __name__ == "__main__":
    app.run(debug=True)
