# cheese-quizz

A fun cheese quizz deployed on OpenShift and illustrating cloud native technologies like Quarkus, Istio Service Mesh, CodeReady Workspaces, Strimzi Kafka Operator, Fuse Online/Syndesis, Tekton pipelines and ArgoCD.

[![Contribute](https://che.openshift.io/factory/resources/factory-contribute.svg)](https://codeready-workspaces.apps.cluster-paris-a2cb.paris-a2cb.example.opentlc.com/f?url=https://github.com/lbroudoux/cheese-quizz)


## Setup

Plese initialize and configure following components in this order:

* A `cheese-quizz` project for holding your project component

* Istio Service Mesh deployed with `basic-install` on `istio-system` project
** Also deploy a `ServiceMeshMemberRoll` into `istio-system` referencing `cheese-quizz` project as member
** Take care of removing `LimitRanges` into `cheese-quizz` project

* Knative Serving deployed cluster wide
** Create a `KnativeServing` CR into `knative-serving` project, adding `image-registry.openshift-image-registry.svc:5000` into `registriesSkippingTagResolving` property

* Fuse Online operator deployed into `fuse-online` project
** Create a `SyndesisCRD` CR, calling it `syndesis`

* CodeReady Workspaces deployed onto `workspaces` project with:
** `quay.io/lbroudoux/che-plugin-registry:master` as the `pluginRegistryImage`
** `true` for `tlsSupport`
** `CHE_INFRA_KUBERNETES_PVC_WAIT__BOUND: 'false'` as `customCheProperties`