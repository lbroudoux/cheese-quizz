'use strict';

module.exports = function (app) {
  // Insert routes below
  app.use('/api/like', require('./api/like'));

  app.get('/healthz', (request, response) => {
    var client = request.app.get('kafkaClient');
    client.loadMetadata(function(err, result) {
      if (err) {
        return response.sendStatus(500);
      }
      response.sendStatus(200);  
    })
  });

  // All undefined asset or api routes should return a 404
  app.route('/:url(api|auth|components|app|bower_components|assets)/*')
    .get(function (req, res) {
      res.sendStatus(404);
    });

  // All other routes should redirect to the index.html
  app.route('/*')
    .get(function (req, res) {
      res.sendFile(app.get('appPath') + '/index.html');
    });
};