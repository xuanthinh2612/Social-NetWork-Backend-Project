package com.project.socialnetwork.controller;
import com.project.socialnetwork.model.AppUser;
import com.project.socialnetwork.model.Relationship;
import com.project.socialnetwork.model.RelationshipStatus;
import com.project.socialnetwork.service.relationship.IRelationshipService;
import com.project.socialnetwork.service.status.IStatusService;
import com.project.socialnetwork.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/relationship")
public class RelationshipController {
    @Autowired
    private IRelationshipService relationshipService;
    @Autowired
    private IStatusService statusService;
    @Autowired
    private IUserService userService;

    @ModelAttribute("listStatus")
    public Iterable<RelationshipStatus> showAll() {
        return statusService.findAllStatus();
    }

    @GetMapping("/")
    public ResponseEntity<Iterable<Relationship>> getAll() {
        return new ResponseEntity<>(relationshipService.findAllRelationship(), HttpStatus.OK);
    }

    @PostMapping("/create/{userReceiveId}")
    public ResponseEntity<Relationship> sendFriendRequest(@PathVariable Long userReceiveId) {
        AppUser currentUser = userService.getCurrentUser();
        Relationship relationship = this.checkRelationship(currentUser.getId(), userReceiveId);
        if (relationship == null) {
            Relationship newRelationship = new Relationship(currentUser.getId(), userReceiveId);
            RelationshipStatus status = statusService.findStatusById(1L);
            newRelationship.setStatus(status);
            newRelationship.setUserSendId(currentUser.getId());
            newRelationship.setUserReceiveId(userReceiveId);
            relationshipService.saveRelationship(newRelationship);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/edit/{userSendId}/{statusId}")
    public ResponseEntity<Relationship> HandleFriendRequest(@PathVariable Long userSendId, @PathVariable Long statusId) {
        AppUser currentUser = userService.getCurrentUser();
        Relationship relationship = relationshipService.findRelationshipByUserSendIdAndUserReceiveId(userSendId, currentUser.getId());
        if (relationship.getStatus().getId() != 2) {
            relationship.setStatus(statusService.findStatusById(statusId));
            return new ResponseEntity<>(relationshipService.saveRelationship(relationship), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/unfriend/{userSendId}")
    public ResponseEntity<Relationship> unFriend(@PathVariable Long userSendId) {
        AppUser currentUser = userService.getCurrentUser();
        Relationship relationship = this.checkRelationship(currentUser.getId(), userSendId);
        if (relationship != null) {
            relationshipService.deleteRelationship(relationship.getId());
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/checkFriend/{userSendId}/{userReceiveId}")
    public ResponseEntity<?> checkFriend(@PathVariable Long userSendId, @PathVariable Long userReceiveId) {
        Long status;
        Relationship relationship = this.checkRelationship(userSendId, userReceiveId);
        if (relationship != null) {
            status = relationship.getStatus().getId();
        } else status = 0L;
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @GetMapping("/listFriend/{userId}")
    public ResponseEntity<Iterable<AppUser>> findAllFriend(@PathVariable Long userId) {
        Iterable<Relationship> relationships = relationshipService.findAllByUserSendIdAndStatusOrUserReceiveIdAndStatus(userId, statusService.findStatusById(2L), userId, statusService.findStatusById(2L));
        List<AppUser> users = new ArrayList<>();
        for (Relationship relationship : relationships
        ) {
            if (relationship.getUserSendId().equals(userId)) {
                users.add(userService.findById(relationship.getUserReceiveId()));
            } else users.add(userService.findById(relationship.getUserSendId()));

        }
        return new ResponseEntity<Iterable<AppUser>>(users, HttpStatus.OK);
    }

    public Relationship checkRelationship(Long userSendId, Long userReceiveId) {
        Relationship relationship;
        if (relationshipService.findRelationshipByUserSendIdAndUserReceiveId(userSendId, userReceiveId) != null) {
            relationship = relationshipService.findRelationshipByUserSendIdAndUserReceiveId(userSendId, userReceiveId);
        } else if (relationshipService.findRelationshipByUserSendIdAndUserReceiveId(userReceiveId, userSendId) != null) {
            relationship = relationshipService.findRelationshipByUserSendIdAndUserReceiveId(userReceiveId, userSendId);
        } else relationship = null;
        return relationship;
    }

    @GetMapping("/listPendingFriend/{userId}")
    public ResponseEntity<Iterable<AppUser>> findMutualFriend(@PathVariable Long userId) {
        Iterable<Relationship> relationships = relationshipService.findAllByUserReceiveIdAndStatus(userId, statusService.findStatusById(1L));

        List<AppUser> users = new ArrayList<>();
        for (Relationship relationship : relationships
        ) {
            users.add(userService.findById(relationship.getUserSendId()));
        }
        return new ResponseEntity<Iterable<AppUser>>(users, HttpStatus.OK);
    }

//    @GetMapping("/listSimilarFriend/{userId1}/{userId2}")
//    public List<Long> findAllSimilarFriend(@PathVariable Long userId1, @PathVariable Long userId2) {
//        AppUser user1 = userService.findById(userId1);
//        AppUser user2 = userService.findById(userId2);
//        return relationshipService.findAllSimilarFriend(user1,user2);
//    }
}
