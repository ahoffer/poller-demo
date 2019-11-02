### Demonstration of a Polling Service.

This Spring application is built on top of 
[dingye's polling library](https://github.com/dyng/polling)  for Java.

The class `PollingService` is a reusable, HTTP polling polling.service. Once constructed, the method 
`PollingService::poll` is used to status polling an HTTP endpoint. The `poll` method accepts a 
lambda function. The lambda function is passed a RestTemplate for the client cod to use.
The client code must return an instance of `AttemptResult` (a class from the dingye polling library).
Use the `AttemptResults` factory class to create instances of `AttemptResult`

The test the app.poller
* Run the [**status-endpoint**](https://github.com/ahoffer/status-endpoint) application first.
* Then run this Spring application.
* The status of the polling.simulation is printed to the log.