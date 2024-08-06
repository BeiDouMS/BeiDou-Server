package org.gms.model.pojo;

import lombok.Data;
import org.gms.constants.game.NextLevelType;

/**
 * nextLevel上下文
 */
@Data
public class NextLevelContext {
    private NextLevelType levelType;
    private String lastLevel;
    private String nextLevel;
    private String prefix;

    public void clear() {
        this.levelType = null;
        this.lastLevel = null;
        this.nextLevel = null;
        this.prefix = null;
    }
}
