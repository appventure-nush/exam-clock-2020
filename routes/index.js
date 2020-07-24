const CLOCKS = require("../clocks.js");
const express = require('express');
const router = express.Router();

router.get('/', ensureAuthenticated, (req, res) => {
    res.render('index', {sessionID: req.sessionID, user: req.user});
});
router.get('/clocks', ensureAuthenticated, (req, res) => {
    // res.json(CLOCKS, null, '\t');
    res.json(Object.values(CLOCKS).map(clock => {
        return {
            id: clock.clockID,
            name: clock.clockName
        };
    }));
});
router.get('/clock/:clockID', ensureAuthenticated, (req, res) => {
    if (CLOCKS[req.params.clockID] && CLOCKS[req.params.clockID].accepts(req.user.id))
        res.render('panel', {
            clockID: req.params.clockID,
            exams: CLOCKS[req.params.clockID].exams.map(exam => JSON.stringify(exam)).join(", ")
        });
    else res.redirect('/');
});
router.post('/', ensureAuthenticated, (req, res) => {
    let clockID = req.body.clockID;
    if (clockID && CLOCKS[clockID] && CLOCKS[clockID].accepts(req.user.id)) {
        req.session.clockID = clockID;
        res.redirect('/clock/' + clockID);
    } else res.render('index', {error: true});
});

module.exports = router;

function ensureAuthenticated(req, res, next) {
    if (req.isAuthenticated()) return next();
    res.redirect('/login');
}