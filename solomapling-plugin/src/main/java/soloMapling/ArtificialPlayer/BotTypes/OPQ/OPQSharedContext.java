package soloMapling.ArtificialPlayer.BotTypes.OPQ;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blackboard shared between {@link OPQOrchestrator} (writer) and all
 * {@link OPQBot} instances (readers).
 *
 * Bots read freely. The only write bots are allowed to make is
 * {@link #markTaskComplete(int)} — a single-bit "I finished my current
 * sub-task" signal. Everything else is orchestrator-owned.
 *
 * Thread-safety model: volatile for scalars (atomic publish), ConcurrentHashMap
 * for collections. No compound invariants cross fields, so no lock is needed.
 */
public class OPQSharedContext {

    public enum OPQPhase {
        INACTIVE,       // no PQ in progress; bots idle
        RECRUITMENT,    // bots in lobby advertising for party invites
        IN_PARTY_IDLE,  // in party, waiting for leader to enter the PQ
        STAGE_1,        // cloud collection in progress
        STAGE_2,        // music record collection in progress
        EXIT            // teleported to exit lobby, heading back to start
    }

    // ---- Phase & stage flags ------------------------------------------------
    private volatile OPQPhase currentPhase = OPQPhase.INACTIVE;
    private volatile boolean  stage1Complete = false;
    private volatile boolean  stage2Complete = false;
    private volatile boolean  pqActive       = false;

    // ---- Assignments --------------------------------------------------------
    // Stage 1: botId -> reactor oid (the cloud reactor the bot is hunting).
    // Stage 2: botId -> reactor oid (the box reactor the bot is hunting).
    // Stage 2 (legacy): botId -> platformId (string), kept for compatibility.
    private final Map<Integer, Integer> cloudAssignments    = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> boxAssignments      = new ConcurrentHashMap<>();
    private final Map<Integer, String>  platformAssignments = new ConcurrentHashMap<>();

    // ---- Task completion ----------------------------------------------------
    private final Map<Integer, Boolean> botTaskComplete     = new ConcurrentHashMap<>();

    // =========================================================================
    // Reader API (safe for bots to call)
    // =========================================================================

    public OPQPhase getCurrentPhase()          { return currentPhase; }
    public boolean  isStage1Complete()         { return stage1Complete; }
    public boolean  isStage2Complete()         { return stage2Complete; }
    public boolean  isPqActive()               { return pqActive; }

    /** Stage 1 cloud reactor oid this bot has been assigned, or null if none. */
    public Integer getMyCloudAssignment(int botId) {
        return cloudAssignments.get(botId);
    }

    /** Stage 2 box reactor oid this bot has been assigned, or null if none. */
    public Integer getMyBoxAssignment(int botId) {
        return boxAssignments.get(botId);
    }

    public String getMyPlatformAssignment(int botId) {
        return platformAssignments.get(botId);
    }

    public boolean isMyTaskComplete(int botId) {
        return Boolean.TRUE.equals(botTaskComplete.get(botId));
    }

    /** The one write bots are allowed: "I finished the thing you told me to do." */
    public void markTaskComplete(int botId) {
        botTaskComplete.put(botId, Boolean.TRUE);
    }

    // =========================================================================
    // Writer API (orchestrator-only in practice; package-private by convention)
    // =========================================================================

    void setCurrentPhase(OPQPhase phase)        { this.currentPhase = phase; }
    void setStage1Complete(boolean v)           { this.stage1Complete = v; }
    void setStage2Complete(boolean v)           { this.stage2Complete = v; }
    void setPqActive(boolean v)                 { this.pqActive = v; }

    void putCloudAssignment(int botId, Integer reactorOid) {
        if (reactorOid == null) cloudAssignments.remove(botId);
        else                    cloudAssignments.put(botId, reactorOid);
    }

    void putBoxAssignment(int botId, Integer reactorOid) {
        if (reactorOid == null) boxAssignments.remove(botId);
        else                    boxAssignments.put(botId, reactorOid);
    }

    void putPlatformAssignment(int botId, String platformId) {
        if (platformId == null) platformAssignments.remove(botId);
        else                    platformAssignments.put(botId, platformId);
    }

    void clearTaskComplete(int botId)           { botTaskComplete.remove(botId); }

    /** Snapshot views for the orchestrator's completion checks. */
    Map<Integer, Integer> getCloudAssignmentsView()    { return cloudAssignments; }
    Map<Integer, Integer> getBoxAssignmentsView()      { return boxAssignments; }
    Map<Integer, String>  getPlatformAssignmentsView() { return platformAssignments; }
    Map<Integer, Boolean> getTaskCompleteView()        { return botTaskComplete; }

    /**
     * Wipe per-run state so the next OPQ run starts clean.
     * Registered bots (owned by orchestrator) are NOT affected.
     */
    void reset() {
        currentPhase   = OPQPhase.INACTIVE;
        stage1Complete = false;
        stage2Complete = false;
        pqActive       = false;
        cloudAssignments.clear();
        boxAssignments.clear();
        platformAssignments.clear();
        botTaskComplete.clear();
    }
}
