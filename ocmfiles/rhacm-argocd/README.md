# Open Cluster Manager + ArgoCD

Will the release of [Red Hat Advanced Cluster Management 2.3](https://access.redhat.com/documentation/en-us/red_hat_advanced_cluster_management_for_kubernetes/2.3/), there's now a bridge between cluster management features into OCM and GitOps deployment using ArgoCD. Here are below the instructions to demonstrate them using the `cheese-quizz` application.

## Cluster setup

### ManagedClusterSet

`ManagedClusterSet` is a new custom resource in OCM that obviously allows to group managed cluster together within a set. For this demonstration, we'll create a set called `clusterset-prod`. This is a cluster wide resource so you can create it directly from the RHACM GUI or with:

```
oc apply -f clusterset-prod.yml
```

For this cluster set, we now have to define a `ManagedClusterSetBinding` that is a link between the cluster-wide resource and a specific Kubernetes namespace where all the specific resources related to this cluster set will resides. Because, it's an ArgoCD demo, we decide to create an `argocd-clusterset-prod` namespace and apply the binding within:

```
kubectl create ns argocd-clusterset-prod
oc apply -f clusterset-prod-binding.yml -n argocd-clusterset-prod
```

We also have to declare a `Placement` for this cluster set. Placement will provide and indirection between resources that want to address a cluster set without having hard reference to it. They'll refer to the `Placement`instead.

```
oc apply -f clusterset-prod-placement.yml -n argocd-clusterset-prod
```

### ArgoCD configuration

On the Hub cluster, you'll have to install the `OpenShift GitOps Operator`. This can be easily done through the embedded OperatorHub into OpenShift. You have to install the operator globally.

Once Operator is installed on the Hub cluster, create a new ArgoCD installation that will be located into the `argocd-clusterset-prod` project:

```
oc apply -f argocd-clusterset-prod.yml -n argocd-clusterset-prod
```

This ArgoCD installation will use the embedded OpenShift authentication mechanism so you'll may log into the GUI using your cluster username and password.

Now that your ArgoCD is up-and-running, you'll need a final touch to bridge your `ManagedClusterSet` with this ArgoCD instance. This should be done creating a `GitOpsCluster`. A `GitOpsCluster` link together a `ManagedClusterSet` resource and an `ArgoCD` resource thanks to the previously created `Placement`.


### Application configuration

Finally, let's deploy some application on top of ArgoCD! This is done through an `ApplicationSet` resource that defines a template for building Argo's `Application`. We'll define this resource using a [Cluster generator](https://argocd-applicationset.readthedocs.io/en/stable/Generators-Cluster/) that allows automatic discovery of cluster based on the presence of `Secrets` within the namespace. These secrets will be in fact produced by the `GitOpsCLuster` in synchronisation with the members of a watched `ManagedClusterSet`.

Just create the application set with:

```
oc apply -f cheese-quizz-applicationset.yml -n argocd-clusterset-prod
```

## Demonstration scenario