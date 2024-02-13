export default {
    goToCodeRequest: 'I want an activation code',
    requestCodeExplanation: `An activation code allows sending SMS messages to MADA dispatch.
If you Are not a MADA volunteer, you do not need a code!`,
    requestCodeInfoHint: 'Enter the name of the MADA region & station you belong to, as well as your medical training level.',
    emptyEmail: 'Please provide an email address',
    invalidEmail: 'The email address provided is invalid',
    review: 'Please review before sending',
    reviewAlert: 'Please make sure the email address is correct, otherwise we will not be able to reply',
    reviewSender: 'Sent by: ',
    reviewContent: 'Message content: ',
    requestCodeSent: `Your request has arrived at our systems.
We will try responding within a few hours.`,
    partialCode: 'You did not enter the full code!',
    nonRequested: `It seems to us you never asked for a activation code.
If you sent a request once, please request another one, and explain the error that happened.`,
    wrongCode: 'The activation code is wrong or does not exist. Please try again!',
    usedCode: `This code has been used.
A code can be used only once!
There might have been an error. try typing the code manually: {{ code }}`,
    notAllowed: `This code has not yet been approved!
We might have messed this up, try typing the code manually: {{ code }}`,
    codeActivated: `The code has worked!
The app will now work!`,
} as const;
