# cheese-quizz

A fun cheese quizz deployed on OpenShift and illustrating cloud native technologies like Quarkus, Istio Service Mesh, CodeReady Workspaces, Strimzi Kafka Operator, Fuse Online/Syndesis, Tekton pipelines and ArgoCD.

![cheese-quizz-overview](./assets/cheese-quizz-overview.png)

* **Part 1** Try to guess the displayed cheese! Introducing new cheese questions with Canaray Release and making everything resilient and observable using Istio Service Mesh. Deploy supersonic components made in Quarkus,

* **Part 2** Implement a new "Like Cheese" feature in a breeze using CodeReady Workspaces, demonstrate the inner loop development experience and then deploy everything using Tekton.

* **Part 3** Add the "Like Cheese API" using Serverless Knative and make it push new messages to Kafka broker. Use Syndesis or CamelK to deploy new integration services and create lead into Salesforce CRM ;-) 

## Start here for viewing the code

[![Contribute](https://che.openshift.io/factory/resources/factory-contribute.svg)](https://codeready-workspaces.apps.cluster-lemans-0014.lemans-0014.example.opentlc.com/f?url=https://github.com/lbroudoux/cheese-quizz)

## Cluster Setup

Please initialize and configure following operators in this order:

> All the components below can be setup using my `cluster-init.sh` script that you may find [here](https://github.com/lbroudoux/openshift-cases/tree/master/cluster-init/ocp-4.4).

* Istio Service Mesh deployed with `basic-install` on `istio-system` project
  * Take care of removing `LimitRanges` into `cheese-quizz` project

* Knative Serving deployed cluster wide
  * Create a `KnativeServing` CR into `knative-serving` project, adding `image-registry.openshift-image-registry.svc:5000` into `registriesSkippingTagResolving` property

* Fuse Online operator deployed into `fuse-online` project
  * Create a `SyndesisCRD` CR, calling it `syndesis`

* AMQ Streams operator deployed cluster wide,

* OpenShift Pipelines deployed cluster wide,

* CodeReady Workspaces operator deployed onto `workspaces` project with:
  * `quay.io/lbroudoux/che-plugin-registry:master` as the `pluginRegistryImage`
  * `true` for `tlsSupport`
  * `CHE_INFRA_KUBERNETES_PVC_WAIT__BOUND: 'false'` as `server.customCheProperties`

This is what the `CheCluster` custom resource shoud look like: 

```yml
apiVersion: org.eclipse.che/v1
kind: CheCluster
metadata:
  name: codeready-workspaces
  namespace: workspaces
spec:
  server:
    cheImageTag: ''
    cheFlavor: codeready
    devfileRegistryImage: ''
    pluginRegistryImage: 'quay.io/lbroudoux/che-plugin-registry:master'
    tlsSupport: true
    selfSignedCert: false
    customCheProperties:
      CHE_INFRA_KUBERNETES_PVC_WAIT__BOUND: 'false'
  database:
    externalDb: false
    chePostgresHostName: ''
    chePostgresPort: ''
    chePostgresUser: ''
    chePostgresPassword: ''
    chePostgresDb: ''
  auth:
    openShiftoAuth: true
    identityProviderImage: ''
    externalIdentityProvider: false
    identityProviderURL: ''
    identityProviderRealm: ''
    identityProviderClientId: ''
  storage:
    pvcStrategy: per-workspace
    pvcClaimSize: 1Gi
    preCreateSubPaths: true
```

## Demo setup

We need to setup the following resources for our demonstration:

* A `cheese-quizz` project for holding your project component
  * Make this project part of the Service Mesh control plane installed into `istio-system` by deploying a `ServiceMeshMemberRoll` into `istio-system` referencing `cheese-quizz` project as member

  * Once done, ensure `cheese-quizz` project has the following labels to be sure it is included into Service Mesh: 
    * `istio.io/member-of=istio-system`
    * `kiali.io/member-of=istio-system`

* A `cheese-quizz-function` project for holding the serverless part and the Kafka broker

* A `Kafka` broker CR into `cheese-quizz-function` letting the default properties
  * `oc create -f manifest/kafka.yml -n cheese-quizz-function`
  * `oc create -f manifest/kafka-topic.yml -n cheese-quizz-function`

Start deploying the components needed at the beginning of this demo, we'll deploy the other ones later on.

```
oc create -f manifests/quizz-question-deployment-v1.yml -n cheese-quizz
oc create -f manifests/quizz-question-deployment-v2.yml -n cheese-quizz
oc create -f manifests/quizz-question-deployment-v3.yml -n cheese-quizz
oc create -f manifests/quizz-question-service.yml -n cheese-quizz
oc apply -f manifests/quizz-question-destinationrule.yml -n cheese-quizz
oc apply -f manifests/quizz-question-virtualservice-v1.yml -n cheese-quizz
oc create -f manifests/quizz-client-buildconfig.yml -n cheese-quizz
oc create -f manifests/quizz-client-deploymentconfig.yml
oc create -f manifests/quizz-client-route.yml
oc rollout latest cheese-quizz-client
```

## Demonstration scenario

Once above commands are issued and everything successfully deployed, retrieve the Cheese Quzz route:

```
$ oc get route/cheese-quizz-client -n cheese-quizz |grep cheese-quizz-client |awk '{print $2}' 
cheese-quizz-client-cheese-quizz.apps.cluster-lemans-0014.lemans-0014.example.opentlc.com
```

and open it into a browser. You shoul get the following:

<img src="./assets/cheddar-quizz.png" width="400">

### OpenShift ServiceMesh demonstration

#### Canary release and blue-green deployment

Introduce new `v2` question using Canary Release and header-matching routing rules:

```
oc apply -f manifests/vs-cheese-quizz-question-virtualservice-v1-v2-canary.yml -n cheese-quizz
```

Using the hamburger menu on the GUI, you should be able to ubscribe the `Beta Program` and see the new Emmental question appear ;-) 

<img src="./assets/emmental-quizz.png" width="400">

Now turning on the `Auto Refresh` feature, you should be able to visualize everything into Kiali, showing how turning on and off the Beta subscription has influence on the visualization of networks routes.

Once we're confident with the `v2` Emmental question, we can turn on Blue-Green deployment process using weighted routes on the Istio `VirtualService`. We apply a 70-30 repartition:

```
oc apply -f manifests/vs-cheese-quizz-question-virtualservice-v1-70-v2-30.yml -n cheese-quizz
```

Of course we can repeat the same kind of process and finally introduce our `v3` Camembert question into the game. Finally, we may choose to route evenly to all the different quizz questions, applying a even load-balancer rules on the `VirtualService`: 

```
oc apply -f manifests/vs-cheese-quizz-question-virtualservice-all.yml -n cheese-quizz
```

#### Circuit breaker and observability

Now let's check some network resiliency features of OpenShift Service Mesh.

Start by simulating some issues on the `v2` deployed Pod. For that, we can remote log to shell and invoke an embedded endpoint that will make the pod fail. Here is bellow the sequence of commands you'll need to adapt and run:

```
$ oc get pods -n cheese-quizz | grep v2
cheese-quizz-question-v2-847df79bd8-9c94t                        2/2     Running     0          5d19h
$ oc rsh cheese-quizz-question-v2-847df79bd8-9c94t
----------- TERMINAL MODE: --------------------
Defaulting container name to greeter-service.
Use 'oc describe pod/cheese-quizz-question-v2-847df79bd8-9c94t -n cheese-quizz' to see all of the containers in this pod.
sh-4.4$ curl localhost:8080/api/cheese/flag/misbehave
Following requests to / will return a 503
sh-4.4$ exit
exit
command terminated with exit code 130
```

Back to the browser window you should now have a little mouse displayed when application tries to reach the `v2` question of the quizz.

<img src="./assets/error-quizz.png" width="400">

Using obervability features that comes with OpenShift Service Mesh like Kiali and Jaeger, you are now able to troubleshoot and check where the problem comes from (imagine that we already forgot we did introduce the error ;-))

Thus you can see the Pod causing troubles with Kiali graph:

![kiali-error-v2](./assets/kiali-error-v2.png)

And inspect Jeager traces to see the details of an error:

![kiali-traces-v2](./assets/kiali-traces-v2.png)

In order to make our application more resilient, we have to start by creating new replicas, so scale the `v2` deployment. 

```
oc scale deployment/cheese-quizz-question-v2 --replicas=2 -n cheese-quizz
```

Newly created pod will serve requests without error but we can see in the Kiali console that the service `cheese-quizz-question` remains degraded (despite green arrows joinining `v2` Pods).

![kiali-degraded-v2](./assets/kiali-degraded-v2.png)

There's still some errors in distributed traces. You can inspect what's going on using Jaeger and may check that there's still some invocations going to the faulty `v2` pod.

![kiali-replay-v2](./assets/kiali-replay-v2.png)

Istio proxies automatically retry doing the invocation to `v2` because a number of conditions are present:
* There's a second replica present,
* It's a HTTP `GET` request that is supposed to be idempotent (so replay is safe),
* We're in simple HTTP with no TLS so the headers inspectation allow determine these conditions.

An optimal way of managing this kind of issue would be to declare a `CircuitBreaker` for handling this problem more efficiently. Circuit breaker policy will be in charge to detect Pod return ing errors and evict them from the elligible targets pool for a configured time. Then, the endpoint will be re-tried and will re-join the pool if erverything is back to normal.

Let's apply the circuit breaker configuration to our question `DestinationRule`:

```
oc apply -f istiofiles/dr-cheese-quizz-question-cb -n cheese-quizz
```

Checking the traces once again in Kiali, you should no longer see any errors! 

#### Timeout/retries management

Pursuing with network resiliency features of OpenShift Service Mesh, let's check now how to handle timeouts.

Start by simulating some latencies on the `v3` deployed Pod. For that, we can remote log to shell and invoke an embedded endpoint that will make the pod slow. Here is bellow the sequence of commands you'll need to adapt and run:

````
$ oc get pods -n cheese-quizz | grep v3
cheese-quizz-question-v3-9cfcfb894-tjtln                         2/2     Running     0          6d1h
$ oc rsh cheese-quizz-question-v3-9cfcfb894-tjtln
----------- TERMINAL MODE: --------------------
Defaulting container name to greeter-service.
Use 'oc describe pod/cheese-quizz-question-v3-9cfcfb894-tjtln -n cheese-quizz' to see all of the containers in this pod.
sh-4.4$ curl localhost:8080/api/cheese/flag/timeout
Following requests to / will wait 3s
sh-4.4$ exit
exit
````

Back to the browser window you should now have some moistures displayed when application tries to reach the `v3` question of the quizz.

<img src="./assets/timeout-quizz.png" width="400">

Before digging and solving this issue, let's review the application configuration :
* A 3 seconds timeout is configured within the Pod handling the `v3` question. Let see the [question source code](https://github.com/lbroudoux/cheese-quizz/blob/master/quizz-question/src/main/java/com/github/lbroudoux/cheese/CheeseResource.java#L110)
* A 1.5 seconds timeout is configured within the Pod handling the client. Let see the [client configuration](https://github.com/lbroudoux/cheese-quizz/blob/master/quizz-client/src/main/resources/application.properties#L14)

Checking the distributed traces within Kiali console we can actually see that the request takes 1.5 seconds before returning an error:

![kiali-timeout-v3](./assets/kiali-timeout-v3.png)

In order to make our application more resilient, we have to start by creating new replicas, so scale the `v3` deployment.

```
oc scale deployment/cheese-quizz-question-v3 --replicas=2 -n cheese-quizz
```

Newly created pod will serve requests without timeout but we can see in the Kiali console that the service `cheese-quizz-question` remains degraded (despite green arrows joinining `v3` Pods).

However there's still some errors in distributed traces. You can inspect what's going on using Jaeger and may check that there's still some invocations going to the slow `v3` pod.

The `CircuitBreaker` policy applied previsouly does not do anything here because the issue is not an application problem that can be detected by Istio proxy. The result of a timed out invocation remains uncertain, but we know that in our case - an idempotent `GET` HTTP request - we can retry the invocation.

Let's apply for this a new `VirtualService` policy that will involve a retry on timeout.

```
oc apply -f istiofiles/vs-cheese-quizz-question-all-retry-timeout.yml -n cheese-quizz
```

Once applied, you should not see errors on the GUI anymore. When digging deep dive into the distributed traces offered by OpenShift Service Mesh, you may however see errors traces. Getting into the details, you see that detailed parameters of the `VirtualService` are applied: Istio do not wait longer than 100 ms before making another attempt and finally reaching a valid endpoint.

![kiali-all-cb-timeout-retry-traces](./assets/kiali-all-cb-timeout-retry-traces.png)

The Kiali console grap allow to check that - from a end user point of view - the service is available and green. We can see that time-to-time the HTTP throughput on `v3` may be reduced due to some failing attempts but we have now great SLA even if we've got one `v2` Pod failing and one `v3` Pod having response time issues:

![kiali-all-cb-timeout-retry](./assets/kiali-all-cb-timeout-retry.png)

#### Security with MTLS

Let's try applying Mutual TLS on our destinations:

```
oc apply -f istiofiles/dr-cheese-quizz-question-mtls -n cheese-quizz
```

### CodeReady Workspaces demonstration

![crw-workspace-creation](./assets/crw-workspace-creation.png)

![crw-workspace](./assets/crw-workspace.png)

![crw-model-install](./assets/crw-model-install.png)

![crw-model-question-compile](./assets/crw-question-compile.png)

![crw-model-client-compile](./assets/crw-client-compile.png)

![crw-model-client-updated-preview](./assets/crw-client-updated-preview.png)

### OpenShift Pipelines demonstration

Ensure the different custom resources for Tekton / OpenShift Pipelines are installed into the `cheese-quizz` project:

```
oc create -f manifests/oc-deploy-task.yml -n cheese-quizz
oc create -f manifests/oc-ensure-no-triggers.yml -n cheese-quizz
oc create -f manifests/oc-patch-deployment-task.yml -n cheese-quizz
oc create -f manifests/oc-start-build-task.yml -n cheese-quizz
oc create -f manifests/quizz-client-pipeline.yml -n cheese-quizz
oc create -f manifests/quizz-client-pipeline-trigger.yml -n cheese-quizz
oc create -f manifests/quizz-client-pipeline-listener.yml -n cheese-quizz
``` 

Configure a `Webhook` trigger on your Git repository holding the sources. First you have to retrieve the full URL of the Tekton `Trigger` that must be invoked:

```
oc get route/quizz-client-pipeline-listener -n cheese-quizz | grep quizz-client-pipeline-listener | awk '{print $2}'
```

Then follow your preferred Git repo documentation to set such a webhook. Here's an example below using GitHub on this repository: 

![tekton-github-trigger](./assets/tekton-github-trigger.png)

Now that this part is OK, you can finish your work into CodeReady Workspaces by commiting the changed file and pushing to your remote repository:

![crw-git-push](./assets/crw-git-push.png)

And this should simply trigger the Tekton / OpenShift Pipeline we just created !

![tekton-pipeline-trigger](./assets/tekton-pipeline-trigger.png)

![tekton-pipeline-logs](./assets/tekton-pipeline-logs.png)

![tekton-pipeline-success](./assets/tekton-pipeline-success.png)

### OpenShift Serverless demonsration

### Fuse Online demonstration

$ oc -n argocd apply -f https://raw.githubusercontent.com/argoproj/argo-cd/v1.4.2/manifests/install.yaml


{"email":"david.clauvel@gmail.com","username":"David Clauvel","cheese":"Cheddar"}