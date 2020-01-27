'use strict';

const express = require('express'),
  path = require('path'),
  bodyParser = require('body-parser'),
  cors = require('cors');

var kafka = require('kafka-node'),
  Producer = kafka.Producer;

// Retrieve config
const port = process.env.PORT || 4000;
const kafkaHost = process.env.KAFKA_HOST || 'localhost:9092';
const kafkaLikeTopic = process.env.KAFKA_LIKE_TOPIC || 'cheese-quizz-likes';

// Initialize Kafka producer
var client = new kafka.KafkaClient({kafkaHost: kafkaHost});
var producer = new Producer(client);

// Setup server
const app = express();
app.use(bodyParser.json());
app.use(cors());

app.set('kafkaProducer', producer);
app.set('kafkaLikeTopic', kafkaLikeTopic);

// Start server if connection to kafka is OK.
producer.on('ready', function () {
  console.log("Kafka Producer is connected and ready.");

  // Then configure other API routes
  require('./routes')(app);

  const server = app.listen(port, '0.0.0.0', function () {
    console.log('Express server listening on port ' + port);
  });
});