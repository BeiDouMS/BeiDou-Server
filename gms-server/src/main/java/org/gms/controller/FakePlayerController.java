package org.gms.controller;

import org.gms.client.fake.FakePlayer;
import org.gms.client.fake.FakePlayerConfig;
import org.gms.client.fake.FakePlayerManager;
import org.gms.client.fake.FakePlayerRandomizer;
import org.gms.service.FakePlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 假人管理 API 控制器
 * <p>
 * 提供 REST API 来管理假人系统
 * </p>
 *
 * @author BeiDou
 * @since 1.0.0
 */
@Tag(name = "假人管理", description = "假人系统的管理接口")
@RestController
@RequestMapping("/api/latest/fake-player")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fake-player", name = "enabled", havingValue = "true")
public class FakePlayerController {

    private final FakePlayerManager manager = FakePlayerManager.getInstance();
    private final FakePlayerService service;
    private final FakePlayerConfig config;

    @Operation(summary = "获取假人系统状态")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", config.isEnabled());
        status.put("totalFakes", manager.getTotalFakeCount());
        status.put("configuredMaps", config.getMaps());
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "在指定地图生成假人")
    @PostMapping("/spawn/{mapId}")
    public ResponseEntity<List<FakePlayer>> spawnFakes(
            @Parameter(description = "地图 ID") @PathVariable int mapId,
            @Parameter(description = "生成数量") @RequestParam(defaultValue = "1") int count) {

        if (count <= 0 || count > 20) {
            return ResponseEntity.badRequest().build();
        }

        List<FakePlayer> spawned = manager.spawnFakes(mapId, count, null);
        return ResponseEntity.ok(spawned);
    }

    @Operation(summary = "生成随机假人")
    @PostMapping("/spawn/random/{mapId}")
    public ResponseEntity<FakePlayer> spawnRandom(
            @Parameter(description = "地图 ID") @PathVariable int mapId) {

        FakePlayer fake = manager.spawnFake(mapId);
        return ResponseEntity.ok(fake);
    }

    @Operation(summary = "获取指定地图的假人列表")
    @GetMapping("/map/{mapId}")
    public ResponseEntity<List<FakePlayer>> getFakesInMap(
            @Parameter(description = "地图 ID") @PathVariable int mapId) {
        return ResponseEntity.ok(manager.getFakesInMap(mapId));
    }

    @Operation(summary = "获取假人详情")
    @GetMapping("/{fakeId}")
    public ResponseEntity<FakePlayer> getFake(
            @Parameter(description = "假人 ID") @PathVariable int fakeId) {
        FakePlayer fake = manager.getFake(fakeId);
        if (fake != null) {
            return ResponseEntity.ok(fake);
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "移除指定地图的所有假人")
    @DeleteMapping("/map/{mapId}")
    public ResponseEntity<Map<String, Integer>> removeAllFakes(
            @Parameter(description = "地图 ID") @PathVariable int mapId) {
        int removed = manager.removeAllFakes(mapId);
        Map<String, Integer> result = new HashMap<>();
        result.put("removed", removed);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "移除单个假人")
    @DeleteMapping("/{fakeId}")
    public ResponseEntity<Void> removeFake(
            @Parameter(description = "假人 ID") @PathVariable int fakeId) {
        if (manager.removeFake(fakeId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "刷新指定地图的假人")
    @PostMapping("/refresh/{mapId}")
    public ResponseEntity<Map<String, Object>> refreshFakes(
            @Parameter(description = "地图 ID") @PathVariable int mapId) {
        int oldCount = manager.getFakesInMap(mapId).size();

        manager.refreshFakes(mapId);

        Map<String, Object> result = new HashMap<>();
        result.put("oldCount", oldCount);
        result.put("newCount", manager.getFakesInMap(mapId).size());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "预览随机生成的假人（不实际生成）")
    @GetMapping("/preview")
    public ResponseEntity<FakePlayer> previewRandom() {
        FakePlayer preview = FakePlayerRandomizer.generateRandom();
        return ResponseEntity.ok(preview);
    }
}
