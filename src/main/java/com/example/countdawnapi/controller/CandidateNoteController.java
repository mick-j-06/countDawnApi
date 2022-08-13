package com.example.countdawnapi.controller;

import com.example.countdawnapi.Service.CandidateNoteService;
import com.example.countdawnapi.Service.NoteService;
import com.example.countdawnapi.Service.StatusService;
import com.example.countdawnapi.model.Candidate;
import com.example.countdawnapi.model.CandidateNote;
import com.example.countdawnapi.model.Status;
import com.example.countdawnapi.repository.StatusRepository;
import com.example.countdawnapi.repository.CandidateNoteRepository;
import com.example.countdawnapi.repository.CandidateRepository;
import com.example.countdawnapi.repository.NoteRepository;
import com.example.countdawnapi.model.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/candidates-notes")
public class CandidateNoteController {
    @Autowired
    private CandidateNoteRepository candidateNoteRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private StatusRepository statusRepository;

    @GetMapping(value = "")
    public List<CandidateNote> showAll(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(name = "status", required = false) String status
    ) {
        if (status != null) {
            return candidateNoteRepository.findAllByStatus(status);
        }
        return candidateNoteRepository.findAll(PageRequest.of(page, size)).toList();
    }

    @PostMapping(value = "")
    public CandidateNote createCandidateNote(@RequestBody CandidateNote candidateNote) {
        Candidate candidateRequestBody = candidateRepository.save(candidateNote.getCandidate());
        Note noteRequestBody = NoteService.moyenne(candidateNote.getNote());
        Note note = noteRepository.save(noteRequestBody);
        CandidateNote newCandidateNote = new CandidateNote();
        newCandidateNote.setCandidate(candidateRequestBody);
        newCandidateNote.setNote(note);
        candidateNoteRepository.save(candidateNote);
        return candidateNote;
    }

    @PutMapping(value = "/status")
    public Status updateStatus(@RequestBody(required = false) Status status) {
        if (status == null) {
            status = new Status();
            status.setAdmitted(10);
            status.setPending(8);
            status.setRecaler(6);
        }
        Status newStatus = StatusService.updateStatus(statusRepository.findById(1).get(), status);
        statusRepository.save(newStatus);
        List<CandidateNote> candidateNoteList = candidateNoteRepository.findAll();
        List<CandidateNote> newCandidateNoteList = CandidateNoteService.updateStatus(
                candidateNoteList,
                newStatus.getAdmitted(),
                newStatus.getPending(),
                newStatus.getRecaler()
        );
        candidateNoteRepository.saveAll(newCandidateNoteList);
        return status;
    }

    @DeleteMapping(value = "/delete/{id}")
    public void deleteCandidateNote(@PathVariable int id) {
        CandidateNote candidateNote = candidateNoteRepository.findById(id).get();
        candidateRepository.deleteById(candidateNote.getCandidate().getId());
        noteRepository.deleteById(candidateNote.getNote().getId());
        candidateNoteRepository.deleteById(id);
    }

}
