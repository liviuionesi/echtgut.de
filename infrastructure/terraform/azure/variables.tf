variable "location" {
  description = <<-EOT
    Azure region. Pick one with B2ats_v2 + AKS free-tier control plane
    availability. Azure periodically closes specific regions to new
    customers on brand-new subscriptions — re-verify against your own
    subscription rather than assuming any single region stays open
    (lmdb.dev hit exactly this with westeurope on 2026-07-29; eastus
    worked). Not a fixed fact, a live, changing restriction.
  EOT
  type        = string
  default     = "eastus"
}

variable "resource_group_name" {
  type    = string
  default = "echtgut-demo"
}

variable "cluster_name" {
  type    = string
  default = "echtgut-aks"
}

variable "vm_size" {
  description = "Node size for the single demo node. echtgut's cloud slice is one Spring Boot backend + one Postgres statefulset — closer to lmdb.dev's original movie-only slice than its full multi-service set, so this starts at the smaller size that slice used (Standard_D2ls_v7), not the D4ls_v7 lmdb.dev grew into once it added Postgres/Mongo/Redis/Ollama alongside four more services. Re-check your subscription's Dlsv7-family quota/allow-list before first apply — both are subscription-specific and have bitten this exact module before (see modules/cluster-aks/variables.tf)."
  type        = string
  default     = "Standard_D2ls_v7"
}

variable "node_count" {
  type    = number
  default = 1
}

variable "enable_node_public_ip" {
  description = "Gives the node a public IP so the backend is reachable without a Standard LB/NAT gateway (both bill hourly). Small non-zero cost while the cluster is up."
  type        = bool
  default     = true
}

variable "demo_inbound_port" {
  description = "NodePort the backend is exposed on; must match the Service patch in infrastructure/kubernetes/overlays/azure."
  type        = number
  default     = 30080
}

variable "alert_emails" {
  description = "Who gets the zero-spend budget alert. No default — this is a real email, supply it via terraform.tfvars (gitignored) or -var at apply time."
  type        = list(string)
}

variable "budget_amount" {
  description = "Zero-spend tripwire threshold in the subscription's billing currency."
  type        = number
  default     = 1
}

variable "budget_start_date" {
  description = "First of the current month, RFC3339 (e.g. 2026-09-01T00:00:00Z). No default — depends on when you actually apply."
  type        = string
}

variable "tags" {
  type = map(string)
  default = {
    project     = "echtgut"
    managed-by  = "terraform"
    environment = "demo"
  }
}
