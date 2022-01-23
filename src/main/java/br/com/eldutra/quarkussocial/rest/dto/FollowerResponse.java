package br.com.eldutra.quarkussocial.rest.dto;

import br.com.eldutra.quarkussocial.domain.model.Follower;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class FollowerResponse {

    private Long id;
    private String name;

    public FollowerResponse(Follower follower){
        this(follower.getId(), follower.getFollower().getName());

    }

}
