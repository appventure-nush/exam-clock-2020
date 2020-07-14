var express = require('express');
var router = express.Router();

router.get('/', function (req, res) {
    res.render('index', {});
})
router.post('/', function (req, res) {
    console.log(req.body);
    res.render('index', {title: 'Express'});
});

module.exports = router;
