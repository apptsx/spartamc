const { handleMessageDelete } = require('../handlers/messageLogHandler');

module.exports = {
    name: 'messageDelete',
    async execute(message, client) {
        if (message.partial) {
            try {
                message = await message.fetch();
            } catch {
                return;
            }
        }
        await handleMessageDelete(message);
    }
};
