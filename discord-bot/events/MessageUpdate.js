const { handleMessageUpdate } = require('../handlers/messageLogHandler');

module.exports = {
    name: 'messageUpdate',
    async execute(oldMessage, newMessage, client) {
        if (oldMessage.partial) {
            try {
                oldMessage = await oldMessage.fetch();
            } catch {
                return;
            }
        }
        if (newMessage.partial) {
            try {
                newMessage = await newMessage.fetch();
            } catch {
                return;
            }
        }
        await handleMessageUpdate(oldMessage, newMessage);
    }
};
