const CLOCKS = require("../clocks.js");
const express = require('express');
const router = express.Router();

router.get('/', (req, res) => {
    res.render('index', {sessionID: req.sessionID});
});
router.get('/clocks', (req, res) => {
    // res.json(CLOCKS, null, '\t');
    res.json(Object.values(CLOCKS).map(clock => {
        return {
            id: clock.clockID,
            name: clock.clockName
        };
    }));
});
router.get('/:clockID', (req, res) => {
    console.log(CLOCKS[req.params.clockID]);
    if (CLOCKS[req.params.clockID] && CLOCKS[req.params.clockID].accepts(req.session.sessionID)) res.render('panel', {clockID: req.params.clockID});
    else res.redirect('/');
});
router.post('/', (req, res) => {
    let nick = req.body.nick;
    let clockID = req.body.clockID;
    if (clockID && nick && CLOCKS[clockID] && CLOCKS[clockID].accepts(req.session.sessionID)) {
        req.session.clockID = clockID;
        res.redirect('/' + clockID);
    } else res.render('index', {error: true});
});

module.exports = router;
