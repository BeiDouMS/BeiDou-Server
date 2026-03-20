var l10n = {};

l10n.Introduction = ({ requirements, searchEnabled }) => `
#e#b<Party Quest: 1st Accompaniment>#k#n
${requirements}

How about you and your party members collectively beating a quest? \
Here you'll find obstacles and problems where you won't be able to beat it without great teamwork. \
If you want to try it, please tell the #bleader of your party#k to talk to me.#b
#L0#I want to participate in the party quest.
#L1#I would like to ${searchEnabled ? "disable" : "enable"} Party Search.
#L2#I would like to hear more details.");
`;

l10n.Details = `
#e#b<Party Quest: 1st Accompaniment>#k#n
Your party must pass through many obstacles and puzzles while traversing the sub-objectives of this Party Quest. \
Coordinate with your team in order to further advance and defeat the final boss \
and collect the dropped item in order to access the rewards and bonus stage.
`;

l10n.SearchToggled = ({ searchEnabled }) => `
Your Party Search status is now: #b${searchEnabled ? "enabled" : "disabled"}#k. \
Talk to me whenever you want to change it back.
`;

l10n.ConfirmAbandon = `Do you wish to abandon this area?`;

l10n.NoParty = `You can participate in the party quest only if you are in a party.`;

l10n.NotLeader = `Your party leader must talk to me to start this party quest.`;

l10n.NotEligible = `
You cannot start this party quest yet, because either your party is not in the range size, \
some of your party members are not eligible to attempt it or they are not in this map. \
If you're having trouble finding party members, try Party Search.
`;

l10n.ChannelBusy = `
Another party has already entered the #rParty Quest#k in this channel. \
Please try another channel, or wait for the current party to finish.
`;

l10n.EventError = `The Kerning PQ has encountered an error.`;
