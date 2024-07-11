package org.gms.constants.string;

import org.gms.client.Character;

/**
 * @author Drago (Dragohe4rt)
 */
public class LanguageConstants {

    enum Language {
        LANG_PRT(0),
        LANG_ESP(1),
        LANG_ENG(2),
        LANG_CN(3),
        ;

        int lang;

        Language(int lang) {
            this.lang = lang;
        }

        private int getValue() {
            return this.lang;
        }

    }

    public static String[] CPQBlue = new String[4];
    public static String[] CPQError = new String[4];
    public static String[] CPQEntry = new String[4];
    public static String[] CPQFindError = new String[4];
    public static String[] CPQRed = new String[4];
    public static String[] CPQPlayerExit = new String[4];
    public static String[] CPQEntryLobby = new String[4];
    public static String[] CPQPickRoom = new String[4];
    public static String[] CPQExtendTime = new String[4];
    public static String[] CPQLeaderNotFound = new String[4];
    public static String[] CPQChallengeRoomAnswer = new String[4];
    public static String[] CPQChallengeRoomSent = new String[4];
    public static String[] CPQChallengeRoomDenied = new String[4];

    static {
        int lang;

        lang = Language.LANG_PRT.getValue();
        LanguageConstants.CPQBlue[lang] = "Maple Azul";
        LanguageConstants.CPQRed[lang] = "Maple Vermelho";
        LanguageConstants.CPQExtendTime[lang] = "O tempo foi estendido.";
        LanguageConstants.CPQPlayerExit[lang] = " deixou o Carnaval de Monstros.";
        LanguageConstants.CPQError[lang] = "Ocorreu um problema. Favor recriar a sala.";
        LanguageConstants.CPQLeaderNotFound[lang] = "Nao foi possivel encontrar o Lider.";
        LanguageConstants.CPQPickRoom[lang] = "Inscreva-se no Festival de Monstros!\r\n";
        LanguageConstants.CPQChallengeRoomAnswer[lang] = "O grupo esta respondendo um desafio no momento.";
        LanguageConstants.CPQChallengeRoomSent[lang] = "Um desafio foi enviado para o grupo na sala. Aguarde um momento.";
        LanguageConstants.CPQChallengeRoomDenied[lang] = "O grupo na sala cancelou seu desafio.";
        LanguageConstants.CPQFindError[lang] = "Nao foi possivel encontrar um grupo nesta sala.\r\nProvavelmente o grupo foi desfeito dentro da sala!";
        LanguageConstants.CPQEntryLobby[lang] = "Agora voce ira receber desafios de outros grupos. Se voce nao aceitar um desafio em 3 minutos, voce sera levado para fora.";
        LanguageConstants.CPQEntry[lang] = "Voce pode selecionar \"Invocar Monstros\", \"Habilidade\", ou \"Protetor\" como sua tatica durante o Carnaval dos Monstros. Use Tab a F1~F12 para acesso rapido!";

        lang = Language.LANG_ESP.getValue();
        LanguageConstants.CPQBlue[lang] = "Maple Azul";
        LanguageConstants.CPQRed[lang] = "Maple Rojo";
        LanguageConstants.CPQExtendTime[lang] = "El tiempo se ha ampliado.";
        LanguageConstants.CPQPlayerExit[lang] = " ha dejado el Carnaval de Monstruos.";
        LanguageConstants.CPQLeaderNotFound[lang] = "No se pudo encontrar el Lider.";
        LanguageConstants.CPQPickRoom[lang] = "!Inscribete en el Festival de Monstruos!\r\n";
        LanguageConstants.CPQError[lang] = "Se ha producido un problema. Por favor, volver a crear una sala.";
        LanguageConstants.CPQChallengeRoomAnswer[lang] = "El grupo esta respondiendo un desafio en el momento.";
        LanguageConstants.CPQChallengeRoomSent[lang] = "Un desafio fue enviado al grupo en la sala. Espera un momento.";
        LanguageConstants.CPQChallengeRoomDenied[lang] = "El grupo en la sala cancelo su desafio.";
        LanguageConstants.CPQFindError[lang] = "No se pudo encontrar un grupo en esta sala.\r\nProbablemente el grupo fue deshecho dentro de la sala!";
        LanguageConstants.CPQEntryLobby[lang] = "Ahora usted recibira los retos de otros grupos. Si usted no acepta un desafio en 3 minutos, usted sera llevado hacia fuera.";
        LanguageConstants.CPQEntry[lang] = "Usted puede seleccionar \"Invocar Monstruos\", \"Habilidad\", o \"Protector\" como su tactica durante el Carnaval de los Monstruos. Utilice Tab y F1 ~ F12 para acceso rapido!";

        lang = Language.LANG_ENG.getValue();
        LanguageConstants.CPQBlue[lang] = "Maple Blue";
        LanguageConstants.CPQRed[lang] = "Maple Red";
        LanguageConstants.CPQPlayerExit[lang] = " left the Carnival of Monsters.";
        LanguageConstants.CPQExtendTime[lang] = "The time has been extended.";
        LanguageConstants.CPQLeaderNotFound[lang] = "Could not find the Leader.";
        LanguageConstants.CPQError[lang] = "There was a problem. Please re-create a room.";
        LanguageConstants.CPQPickRoom[lang] = "Sign up for the Monster Festival!\r\n";
        LanguageConstants.CPQChallengeRoomAnswer[lang] = "The group is currently facing a challenge.";
        LanguageConstants.CPQChallengeRoomSent[lang] = "A challenge has been sent to the group in the room. Please wait a while.";
        LanguageConstants.CPQChallengeRoomDenied[lang] = "The group in the room canceled your challenge.";
        LanguageConstants.CPQFindError[lang] = "We could not find a group in this room.\r\nProbably the group was scrapped inside the room!";
        LanguageConstants.CPQEntryLobby[lang] = "You will now receive challenges from other groups. If you do not accept a challenge within 3 minutes, you will be taken out.";
        LanguageConstants.CPQEntry[lang] = "You can select \"Summon Monsters\", \"Ability\", or \"Protector\" as your tactic during the Monster Carnival. Use Tab and F1 ~ F12 for quick access!";

        lang = Language.LANG_CN.getValue();
        LanguageConstants.CPQBlue[lang] = "蓝队";
        LanguageConstants.CPQRed[lang] = "红队";
        LanguageConstants.CPQPlayerExit[lang] = "离开了怪物嘉年华。";
        LanguageConstants.CPQExtendTime[lang] = "时间已经延长。";
        LanguageConstants.CPQLeaderNotFound[lang] = "队长不存在！";
        LanguageConstants.CPQError[lang] = "发生了一些错误，请重新创建一个房间。";
        LanguageConstants.CPQPickRoom[lang] = "已报名怪物嘉年华！\r\n";
        LanguageConstants.CPQChallengeRoomAnswer[lang] = "该队伍目前正在挑战中";
        LanguageConstants.CPQChallengeRoomSent[lang] = "已经向房间里的队伍发送了一个挑战，请稍等。";
        LanguageConstants.CPQChallengeRoomDenied[lang] = "房间里的队伍取消了你的挑战。";
        LanguageConstants.CPQFindError[lang] = "该房间没有队伍\r\n也许该队伍已经解散了！";
        LanguageConstants.CPQEntryLobby[lang] = "您现在将收到来自其他队伍的挑战。如果你在3分钟内不接受挑战，你将被淘汰。";
        // todo Ability是什么？先机翻一下
        LanguageConstants.CPQEntry[lang] = "在怪物嘉年华期间，你能使用\"召唤怪物\"，\"策略\" 或者 \"防守\"作为您的战术。使用Tab和F1~F12快速访问！";

    }

    public static String getMessage(Character chr, String[] message) {
        return message[chr.getClient().getLanguage()];
    }
}
