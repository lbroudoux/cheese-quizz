'use strict';

var express = require('express');
var controller = require('./like.controller');

var router = express.Router();

router.post('/', controller.createLike)

module.exports = router;