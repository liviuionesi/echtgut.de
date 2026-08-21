output "resource_group_name" {
  value = module.network.resource_group_name
}

output "cluster_name" {
  value = module.cluster_aks.cluster_name
}

output "kube_config_raw" {
  description = "Fallback kubeconfig from Terraform state. Prefer `az aks get-credentials` — it's the mechanism that actually keeps working across CLI/provider versions."
  value       = module.cluster_aks.kube_config_raw
  sensitive   = true
}

# No node_public_ip / gateway_url outputs — lmdb.dev proved live that the
# natural Terraform-side way to compute these (data.azurerm_public_ips
# against the node resource group) returns the AKS-managed outbound Load
# Balancer's address, not the actual per-node address from
# node_public_ip_enabled (a VMSS instance-level IP that data source can't
# see). Get the real address with `kubectl get nodes -o wide` after
# `az aks get-credentials`. Output demo_inbound_port so only the IP half
# needs to come from kubectl.
output "demo_inbound_port" {
  value = var.demo_inbound_port
}
