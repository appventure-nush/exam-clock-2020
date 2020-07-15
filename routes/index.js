const CLOCKS = require("../clocks.js");
const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
    res.render('index', {sessionID: req.sessionID});
});
router.get('/clocks', (req, res) => {
    res.json(Object.values(CLOCKS).map(clock => {
        return {
            id: clock.clockID,
            name: clock.clockName
        };
    }));
});
router.get('/:clockID', (req, res) => {
    if (CLOCKS[req.body.clockID] && CLOCKS[req.params.clockID].accepts(req.session.sessionID)) res.render('panel', {clockID: req.params.clockID});
    else res.render('index', {error: true});
});
router.post('/', (req, res) => {
    if (req.body.clockID && req.body.nick && CLOCKS[req.body.clockID] && CLOCKS[req.body.clockID].accepts(req.session.sessionID)) {
        req.session.clockID = req.body.clockID;
        res.redirect('/' + req.body.clockID);
    } else res.render('index', {error: true});
});

module.exports = router;
