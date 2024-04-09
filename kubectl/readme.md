### get all pods configured for kube2iam  
kubectl get pods --all-namespaces -o jsonpath="{range .items[?(@.metadata.annotations.iam\.amazonaws\.com/role)]}{.metadata.namespace}{'\t'}{.metadata.name}{'\t'}{.metadata.annotations.iam\.amazonaws\.com/role}{'\n'}{end}"
