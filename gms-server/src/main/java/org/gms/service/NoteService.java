package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.dao.entity.NotesDO;
import org.gms.dao.mapper.NotesMapper;
import org.gms.net.packet.out.ShowNotesPacket;
import org.gms.net.server.Server;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.gms.dao.entity.table.NotesDOTableDef.NOTES_D_O;

@Service
@AllArgsConstructor
@Slf4j
public class NoteService {
    private final NotesMapper notesMapper;

    /**
     * Send normal note from one character to another
     */
    public void sendNormal(String message, String senderName, String receiverName) {
        notesMapper.insertSelective(NotesDO.builder()
                .message(message)
                .from(senderName)
                .to(receiverName)
                .timestamp(Server.getInstance().getCurrentTime())
                .build());
    }

    /**
     * Send note which will increase the receiver's fame by one.
     */
    public void sendWithFame(String message, String senderName, String receiverName) {
        notesMapper.insertSelective(NotesDO.builder()
                .message(message)
                .from(senderName)
                .to(receiverName)
                .timestamp(Server.getInstance().getCurrentTime())
                .fame(1)
                .build());
    }

    /**
     * Show unread notes
     *
     * @param chr Note recipient
     */
    public void show(Character chr) {
        if (chr == null) {
            throw new IllegalArgumentException("Unable to show notes - chr is null");
        }

        List<NotesDO> notesDOList = notesMapper.selectListByQuery(QueryWrapper.create()
                .from(NOTES_D_O)
                .where(NOTES_D_O.DELETED.eq(0))
                .and(NOTES_D_O.TO.eq(chr.getName())));
        if (!notesDOList.isEmpty()) {
            chr.sendPacket(new ShowNotesPacket(notesDOList));
        }
    }

    /**
     * Delete a read note
     *
     * @param noteId Id of note to discard
     * @return Discarded note. Empty optional if failed to discard.
     */
    public Optional<NotesDO> delete(int noteId) {
        try {
            NotesDO notesDO = notesMapper.selectOneById(noteId);
            notesMapper.deleteById(noteId);
            return Optional.of(notesDO);
        } catch (Exception e) {
            log.error("Failed to discard note with id {}", noteId, e);
            return Optional.empty();
        }
    }

}
