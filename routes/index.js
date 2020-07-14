let CLOCKS;
const express = require('express');
const router = express.Router();

router.get('/', function (req, res) {
    res.render('index', {});
});
router.get('/:clockID', function (req, res) {
    res.render('panel', {clockID: req.params.clockID});
});
router.post('/', function (req, res) {
    if (req.body.clockID) {
        req.session.clockID = req.body.clockID;
        res.redirect('/' + req.body.clockID);
    } else res.render('index', {clockIDDontExist: true});
});

module.exports = (clocks) => {
    CLOCKS = clocks;
    return router;
};
