import requests
from requests.auth import HTTPBasicAuth

# Jenkins details
jenkins_url = "http://yourjenkins.example.com"
script_endpoint = "/scriptText"
user = 'your_user'
api_token = 'your_api_token'

import requests
from requests.auth import HTTPBasicAuth
import xml.etree.ElementTree as ET

# Jenkins details
jenkins_url = "http://yourjenkins.example.com"
crumb_issuer_endpoint = "/crumbIssuer/api/xml"
user = 'your_user'
api_token = 'your_api_token'

# Get CSRF crumb
crumb_response = requests.get(
    jenkins_url + crumb_issuer_endpoint, 
    auth=HTTPBasicAuth(user, api_token)
)

if crumb_response.status_code == 200:
    # Parse the XML response
    root = ET.fromstring(crumb_response.text)
    crumb_field = root.find('crumbRequestField').text
    crumb_value = root.find('crumb').text
else:
    print(f"Failed to get crumb: {crumb_response.status_code}")
    print(crumb_response.text)
    exit(1)

# Groovy script you want to execute
groovy_script = """
println('Hello from Remote Groovy Script!')
// Add more Groovy script here as needed
"""

# Prepare the request
url = jenkins_url + script_endpoint
headers = {
    "Content-type": "application/x-www-form-urlencoded"
}
data = {
    "script": groovy_script
}

# Make the request
response = requests.post(url, headers=headers, data=data, auth=HTTPBasicAuth(user, api_token))

# Check response
if response.status_code == 200:
    print("Script executed successfully: ")
    print(response.text)  # Output from script
else:
    print(f"Failed to execute script: {response.status_code}")
    print(response.text)
