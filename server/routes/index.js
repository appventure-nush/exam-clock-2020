const express = require('express');
const router = express.Router();

router.get('/', function (req, res) {
    res.render('index', {});
})
router.post('/', function (req, res) {
    if (req.body.clockID) res.render('panel', {clockID: req.body.clockID});
    else res.render('index', {clockIDDontExist: true});
});

module.exports = router;
