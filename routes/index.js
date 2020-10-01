const CLOCKS = require("../clocks.js");
const express = require('express');
const router = express.Router();

router.get('/', ensureAuthenticated, (req, res) => {
    res.render('index', {sessionID: req.sessionID, user: req.user, query: req.query});
});
router.get('/help', (req, res) => {
    res.render('help', {user: req.user});
});
router.post('/', ensureAuthenticated, (req, res) => {
    let clockID = req.body.clockID;
    if (clockID && CLOCKS[clockID] && CLOCKS[clockID].accepts(req.user.id)) {
        req.session.clockID = clockID;
        res.redirect('/clock/' + clockID);
    } else res.render('index', {error: true, user: req.user});
});

module.exports = router;

function ensureAuthenticated(req, res, next) {
    if (req.isAuthenticated()) return next();
    res.redirect('/login');
}