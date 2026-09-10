const callHandler = require('../handlers/callHandler');

module.exports = {
    name: 'voiceStateUpdate',
    async execute(oldState, newState, client) {
        await callHandler.onVoiceStateUpdate(oldState, newState, client);
    }
};
