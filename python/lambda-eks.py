import boto3
import os

def generate_kubeconfig(cluster_name, region='us-west-2'):
    # Create an EKS client
    eks_client = boto3.client('eks', region_name=region)
    
    # Get cluster details
    cluster_info = eks_client.describe_cluster(name=cluster_name)['cluster']
    cluster_endpoint = cluster_info['endpoint']
    cluster_ca = cluster_info['certificateAuthority']['data']
    
    # Generate a kubeconfig file
    kubeconfig_contents = f"""
    apiVersion: v1
    clusters:
    - cluster:
        server: {cluster_endpoint}
        certificate-authority-data: {cluster_ca}
      name: kubernetes
    contexts:
    - context:
        cluster: kubernetes
        user: aws
      name: aws
    current-context: aws
    kind: Config
    preferences: {{}}
    users:
    - name: aws
      user:
        exec:
          apiVersion: client.authentication.k8s.io/v1alpha1
          command: aws
          args:
            - "eks"
            - "get-token"
            - "--cluster-name"
            - "{cluster_name}"
            - "--region"
            - "{region}"
          env: null
    """
    
    # Write kubeconfig to temporary file
    kubeconfig_path = '/tmp/kubeconfig'
    with open(kubeconfig_path, 'w') as f:
        f.write(kubeconfig_contents)
    
    # Return the path to the kubeconfig file
    return kubeconfig_path

def lambda_handler(event, context):
    cluster_name = 'your-cluster-name'
    region = 'your-cluster-region'
    
    # Generate kubeconfig
    kubeconfig_path = generate_kubeconfig(cluster_name, region=region)
    
    # Set KUBECONFIG environment variable to use with kubectl
    os.environ['KUBECONFIG'] = kubeconfig_path
    
    # Your logic here to interact with the cluster using kubectl or Kubernetes Python client
    
    return {
        'statusCode': 200,
        'body': 'Successfully configured kubeconfig'
    }
