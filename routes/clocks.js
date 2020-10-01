const CLOCKS = require("../clocks.js");
const express = require('express');
const router = express.Router();

router.get('/', ensureAuthenticated, (req, res) => {
    res.json(Object.values(CLOCKS).map(clock => {
        return {
            id: clock.clockID,
            name: clock.clockName
        };
    }));
});
router.get('/:clockID', ensureAuthenticated, (req, res) => {
    if (CLOCKS[req.params.clockID] && CLOCKS[req.params.clockID].accepts(req.user.id))
        res.render('panel', {
            user: req.user,
            clockID: req.params.clockID,
            exams: CLOCKS[req.params.clockID].exams.map(exam => JSON.stringify(exam)).join(", ")
        });
    else res.redirect('/?clockNotFound=true');
});
module.exports = router;

function ensureAuthenticated(req, res, next) {
    if (req.isAuthenticated()) return next();
    res.redirect('/login');
}