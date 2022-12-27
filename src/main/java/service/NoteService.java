package service;

import client.Character;
import database.DaoException;
import database.note.NoteDao;
import model.Note;
import net.packet.Packet;
import net.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.PacketCreator;

import java.util.List;
import java.util.Optional;

public class NoteService {
    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    private final NoteDao noteDao;

    public NoteService(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    /**
     * Send normal note from one character to another
     *
     * @return Send success
     */
    public boolean sendNormal(String message, String senderName, String receiverName) {
        Note normalNote = Note.createNormal(message, senderName, receiverName, Server.getInstance().getCurrentTime());
        return send(normalNote);
    }

    /**
     * Send note which will increase the receiver's fame by one.
     *
     * @return Send success
     */
    public boolean sendWithFame(String message, String senderName, String receiverName) {
        Note noteWithFame = Note.createGift(message, senderName, receiverName, Server.getInstance().getCurrentTime());
        return send(noteWithFame);
    }

    private boolean send(Note note) {
        try {
            NoteDao.save(note);
            return true;
        } catch (DaoException e) {
            log.error("Failed to send note {}", note, e);
            return false;
        }
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

        final List<Note> notes;
        try {
            notes = noteDao.findAllByTo(chr.getName());
        } catch (DaoException e) {
            log.error("Failed to find notes sent to chr name {}", chr.getName(), e);
            return;
        }

        Packet showNotesPacket = PacketCreator.showNotes(notes);
        chr.sendPacket(showNotesPacket);
    }

    /**
     * Discard a read note
     *
     * @param id Id of note to discard
     * @return Discarded note. Empty optional if failed to discard.
     */
    public Optional<Note> discard(int id) {
        try {
            return noteDao.delete(id);
        } catch (DaoException e) {
            log.error("Failed to discard note with id {}", id, e);
            return Optional.empty();
        }
    }


}
