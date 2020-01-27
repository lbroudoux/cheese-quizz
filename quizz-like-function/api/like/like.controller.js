'use strict';

exports.createLike = function (req, res) {
  var like = req.body;
  console.debug("-- Invoking the createLike API with " + JSON.stringify(like));

  var producer = req.app.get('kafkaProducer');
  var kafkaLikeTopic = req.app.get('kafkaLikeTopic');

  const buffer = new Buffer.from(JSON.stringify(like));
  let payloads = [{ topic: kafkaLikeTopic, messages: buffer }]
  
  producer.send(payloads, function (err, data) {
    console.log(err, data);
    if (err) {
      res.status(500).send(JSON.stringify({
        "messages": "Error while sending messages.",
      }));
    } else {
      res.status(201).send(JSON.stringify({
        "messages": "Message sent."
      }));
    }
  });
};