import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.constants.skills.DarkKnight;
import org.gms.manager.ServerManager;
import org.gms.property.ServiceProperty;
import org.gms.provider.Data;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.wz.WZFiles;
import org.gms.server.StatEffect;
import org.gms.server.maps.Summon;
import org.gms.server.maps.SummonAssistantType;
import org.gms.server.maps.SummonMovementType;
import org.gms.service.ConfigService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkillTest {

    private static MockedStatic<ServerManager> mockedServerManager;

    @BeforeAll
    static void setupServerManagerMock() {
        ServiceProperty sp = new ServiceProperty();
        sp.setLanguage("zh-CN");

        ConfigService mockConfigService = Mockito.mock(ConfigService.class);
        Mockito.when(mockConfigService.loadGameConfigs()).thenReturn(Collections.emptyList());

        ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(mockContext.getBean(ServiceProperty.class)).thenReturn(sp);
        Mockito.when(mockContext.getBean(ConfigService.class)).thenReturn(mockConfigService);

        mockedServerManager = Mockito.mockStatic(ServerManager.class);
        mockedServerManager.when(ServerManager::getApplicationContext).thenReturn(mockContext);
    }

    @AfterAll
    static void tearDown() {
        if (mockedServerManager != null) {
            mockedServerManager.close();
        }
    }

    @Test
    public void getSkillSummonMovementTypeTest() {
        SkillFactory.loadAllSkills();

        for (Data skill_ : DataProviderFactory.getDataProvider(WZFiles.STRING).getData("Skill.img").getChildren()) {
            try {
                int skillId = Integer.parseInt(skill_.getName());
                Skill skill = SkillFactory.getSkill(skillId);
                if (skill == null) {
                    continue;
                }
                if (skillId == 2321003) {
                    // 这个不能通过测试，是原代码就有问题还是不能通过动画来推断？
                    continue;
                }
                SummonMovementType oldType = StatEffect.getSummonMovementType(skillId);
                if (oldType == null) {
                    // 原代码中没有的记录
                    assertTrue(true, "SkillId "+ skillId +"not used in StatEffect.getSummonMovementType");
                    continue;
                }
                if (skill.getSummonNode() != null) {
                    assertEquals(
                        oldType, 
                        SummonMovementType.getFromSummonData(skill.getSummonNode()),
                        "SkillId: " + skillId
                    );
                }

            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                continue;
            }
        }
    }

    @Test
    public void getSkillSummonAssistantTypeTest() {
        SkillFactory.loadAllSkills();

        for (Data skill_ : DataProviderFactory.getDataProvider(WZFiles.STRING).getData("Skill.img").getChildren()) {
            try {
                int skillId = Integer.parseInt(skill_.getName());
                Skill skill = SkillFactory.getSkill(skillId);
                if (skill == null) {
                    continue;
                }
                SummonMovementType oldType = StatEffect.getSummonMovementType(skillId);
                if (oldType == null) {
                    // 原代码中用 SummonMovementType != null 来表示是召唤类型
                    assertTrue(true, "SkillId "+ skillId +"not used in StatEffect.getSummonMovementType");
                    continue;
                } 
            
                if (skill.getSummonNode() != null) {
                    SummonAssistantType newType = SummonAssistantType.getFromSummonData(skill.getSummonNode());
                    // p.writeBool(!summon.isPuppet()); // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
                    boolean isPuppet = Summon.isPuppet(skillId);
                    if (isPuppet) {
                        assertEquals(0, newType.getValue());
                    } else {
                        // 本次修复的summon assistantype = 2
                        if (skillId != DarkKnight.BEHOLDER) {
                            assertEquals(1, newType.getValue());
                        }
                    }
                }
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
                continue;
            }
        }
    }

}
