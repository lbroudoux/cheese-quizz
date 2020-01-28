# cheese-quizz

A fun cheese quizz deployed on OpenShift and illustrating cloud native technologies like Quarkus, Istio Service Mesh, CodeReady Workspaces, Strimzi Kafka Operator, Fuse Online/Syndesis, Tekton pipelines and ArgoCD.

[![Contribute](https://che.openshift.io/factory/resources/factory-contribute.svg)](https://codeready-workspaces.apps.cluster-paris-a2cb.paris-a2cb.example.opentlc.com/f?url=https://github.com/lbroudoux/cheese-quizz)


https://codeready-workspaces.apps.cluster-paris-a2cb.paris-a2cb.example.opentlc.com/f?url=https://github.com/mcouliba/cloud-native-workshop


## Setup

Plese initialize and configure following components in this order:

* 
* A `cheese-quizz` project for holding your project component
* Istio Service Mesh deployed with `basic-install` on `istio-system` project
** Also deploy a `ServiceMeshMemeberRoll` into `istio-system` referencing `cheese-quizz` project as member
** Take care of removing `LimitRanges` into `cheese-quizz` project
* CodeReady Workspaces deployed onto `workspaces` project with:
** `quay.io/mcouliba/che-plugin-registry:7.3.x` as the `pluginRegistryImage`
** `true` for `tlsSupport`
** `CHE_INFRA_KUBERNETES_PVC_WAIT__BOUND: 'false'` as `customCheProperties`