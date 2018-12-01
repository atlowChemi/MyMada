let functions = require('firebase-functions');
let admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const gmailEmail = encodeURIComponent(functions.config().gmail.email);
const gmailPassword = encodeURIComponent(functions.config().gmail.password);
admin.initializeApp(functions.config().firebase);
exports.sendPush = functions.database.ref('/codes/{key}').onWrite(event => {
    var snap = event.data;
    var theKey = event.params.key;
    var reas = snap.child("reason").val();
    var allowed = snap.child("allowed").val();
    var used = snap.child("used").val();
    var token= snap.child("token").val();
    var theCode = snap.child("code").val();
    var name = snap.child("name").val();
    var payloadA = {data: {title: name,msg: reas,code: "",key:theKey,type: "newC"}};
    var payloadB = {data: {code:theCode ,key:theKey}};
    if (!event.data.previous.exists()) {
       console.log('ok:', theKey + " was now allowed!");
       return admin.messaging().sendToTopic("admin-topic", payloadA);
    }else if(!event.data.exists()) {
       console.log('deleted:', theKey + " was deleted!");
    }else {
       if(allowed != event.data.previous.child("allowed").val() && allowed) {
          return admin.messaging().sendToDevice(token, payloadB);
       }else if(used) {
          console.log('used:', "was just used!");
       }else {
          console.log('not important:', "non-relevant changes happened");
       }
    }
});

exports.date = functions.https.onRequest((req, res) => {
   if(req.method === "POST") {
      var email = req.body.email;
      var name = req.body.name;
      var subject = req.body.subject;
      var message = req.body.message;
      if (email === undefined || name === undefined || subject === undefined || message === undefined) {
         res.status(400).send('Not enough info defined!');
      } else {
         if(sendTestEmail("1", email, name, subject, message)) {
            var sub = subject;//"=?windows-1255?Q?&#x05D0?=";
            var gotYouMail = "<p style='text-align:right;direction:rtl;'>";
            gotYouMail += "&#1492;&#1493;&#1491;&#1506;&#1514;&#1498; &#1492;&#1514;&#1511;&#1489;&#1500;&#1492; &#1489;&#1502;&#1506;&#1512;&#1499;&#1514;. &#1504;&#1513;&#1514;&#1491;&#1500; &#1500;&#1506;&#1504;&#1493;&#1514; &#1500;&#1498; &#1489;&#1492;&#1511;&#1491;&#1501; &#1493;&#1500;&#1496;&#1508;&#1500; &#1489;&#1489;&#1511;&#1513;&#1514;&#1498; &#1489;&#1488;&#1493;&#1508;&#1503; &#1492;&#1502;&#1497;&#1496;&#1489;&#1497;.<br /><br />";
            gotYouMail += "&#1514;&#1493;&#1491;&#1492; &#1512;&#1489;&#1492; &#1493;&#1497;&#1493;&#1501; &#1496;&#1493;&#1489;.<br /><br /><b>&#1508;&#1504;&#1497;&#1497;&#1514;&#1498; &#1499;&#1508;&#1497; &#1513;&#1492;&#1490;&#1497;&#1506;&#1492; &#1488;&#1500;&#1497;&#1504;&#1493;:</b><br /><br />";
            gotYouMail += message + "<br /><br /><br />";
            gotYouMail += "<b>--&#1510;&#1493;&#1493;&#1514; &#1491;&#1497;&#1493;&#1493;&#1495;&#1497; &#1502;&#1490;&#1503; &#1491;&#1493;&#1491; &#1488;&#1491;&#1493;&#1501; &#1497;&#1512;&#1493;&#1513;&#1500;&#1497;&#1501;.<br /><br /><p style='color:red;direction:rtl;text-align:right;'>&#1494;&#1493;&#1492;&#1497; &#1492;&#1493;&#1491;&#1506;&#1492; &#1488;&#1493;&#1496;&#1493;&#1502;&#1496;&#1497;&#1514; &#1493;&#1488;&#1497;&#1503; &#1500;&#1492;&#1513;&#1497;&#1489; &#1500;&#1492;.</p></b></p>";
            sendTestEmail("2", email, name, sub, gotYouMail);
         }
         res.status(200).send('true');
      }
   }else {
      res.status(400).send('Not post method!');
   }
});

function sendTestEmail(type, email, name, sub, message) {
  var msg = "<p style='direction:rtl;text-align:right;'>" + message + "</p>";
  const mailTransport = nodemailer.createTransport({service: 'gmail',auth: {user: 'atlowapps@gmail.com',pass: 'lnupubtkqbitsmed'}});
  var fromDetails = "&#1491;&#1497;&#1493;&#1493;&#1495;&#1497; &#1502;&#1490;&#1503; &#1491;&#1493;&#1491; &#1488;&#1491;&#1493;&#1501;<atlowapps@gmail.com>";
  var mailOptions;
  
  if(type == "1") {
     mailOptions = {from: fromDetails, to: 'atlowapps@gmail.com', subject: sub, text: message, html: msg, replyTo: name + '<' + email + '>'};
  }else {
     mailOptions = {from: fromDetails, to: name + '<' + email + '>', subject: sub, text: message, html: message};
  }
  return mailTransport.sendMail(mailOptions).then(() => {
     return true;
  });

}





