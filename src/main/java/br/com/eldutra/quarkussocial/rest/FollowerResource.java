package br.com.eldutra.quarkussocial.rest;

import br.com.eldutra.quarkussocial.domain.model.Follower;
import br.com.eldutra.quarkussocial.domain.repository.FollowerRepository;
import br.com.eldutra.quarkussocial.domain.repository.UserRepository;
import br.com.eldutra.quarkussocial.rest.dto.FollowerRequest;
import br.com.eldutra.quarkussocial.rest.dto.FollowerResponse;
import br.com.eldutra.quarkussocial.rest.dto.FollowersPerUserResponse;
import lombok.Data;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Data
public class FollowerResource {


    private FollowerRepository repository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository repository,
                            UserRepository userRepository){

        this.repository = repository;
        this.userRepository = userRepository;
    }
    @PUT
    @Transactional
    public Response followUser( @PathParam("userId") Long userId, FollowerRequest request){

        if(userId.equals(request.getFollowerId())){
            return Response.status(Response.Status.CONFLICT).entity("Corno nao pode").build();
        }
        var user = userRepository.findById(userId);

        if(user == null) {

            return Response.status(Response.Status.NOT_FOUND).build();

        }
        var follower = userRepository.findById(request.getFollowerId());

        boolean follows = repository.follows(follower, user);

        if(!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
    @GET
    public Response ListFollowers(@PathParam("userId") Long userId){

        var user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = repository.findByUser(userId);
        FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
        responseObject.setFollowersCount(list.size());


        var followerList = list.stream().map(FollowerResponse::new)
                .collect(Collectors.toList());
        responseObject.setContent(followerList);
        return Response.ok(responseObject).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser (
            @PathParam("userId") Long userId,
            @QueryParam("followerId") Long followerId ){

        var user = userRepository.findById(userId);
        if(user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.OK).entity(followerId).build();
    }


}
