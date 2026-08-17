package soloMapling.ArtificialPlayer.BotFlavorSystem;

// One ambient idle expression a town/social bot can perform, with its relative pick weight.
// Weights are a starting point (tune via the constants in BotFlavor / a future !env flavor readout).
// EMOTE is cheapest and most universal, so it carries the most weight; the skill/buff flexes are
// rarer punctuation. JITTER / CHAIR_SIT / FLAVOR_CHAT / ITEM_SHOWOFF are planned later phases and are
// intentionally not listed yet - adding an entry here is all it takes to fold one into the roll.
public enum FlavorAction {
    EMOTE(5),
    BUFF_FLEX(3),
    SKILL_SWING(3);

    public final int weight;

    FlavorAction(int weight) {
        this.weight = weight;
    }
}
