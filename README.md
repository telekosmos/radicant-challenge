# Radicant challenge

A simple API done in FP Scala, using Tagless Final and Hexagon architecture.

Suppossedly you have the databas set up (data is not in this repo), you should able to:

`sbt run`

to run the service.

Otherwise, you can do `sbt test` you'll get exceptions trying to connect the backend storage. 

TODO:
- [ ] Implement POST endpoint
- [ ] Implements logical operator parameter for GET endpoint
- [ ] Use environment vars to hide password
- [ ] Use environment vars to choose for running environment
- [ ] Docker everything


