package br.com.eldutra.quarkussocial.rest;

import br.com.eldutra.quarkussocial.domain.model.Post;
import br.com.eldutra.quarkussocial.domain.model.User;
import br.com.eldutra.quarkussocial.domain.repository.FollowerRepository;
import br.com.eldutra.quarkussocial.domain.repository.PostRepository;
import br.com.eldutra.quarkussocial.domain.repository.UserRepository;
import br.com.eldutra.quarkussocial.rest.dto.CreatePostRequest;
import br.com.eldutra.quarkussocial.rest.dto.PostResponse;
import io.quarkus.panache.common.Sort;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResource {

    private UserRepository userRepository;
    private PostRepository repository;
    private FollowerRepository followerRepository;

    @Inject
    public PostResource(UserRepository userRepository,
                        PostRepository repository,
                        FollowerRepository followerRepository){

        this.userRepository = userRepository;
        this.repository = repository;
        this.followerRepository = followerRepository;
    }

    @POST
    @Transactional
    public Response savePost(
            @PathParam("userId") Long userId, CreatePostRequest request){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);

        repository.persist(post);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listPost(
            @PathParam("userId") Long userId,
            @HeaderParam("followerId") Long followerId ){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(followerId == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Lascou followerId").build();
        }

        User follower = userRepository.findById(followerId);

        if(follower == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Lascou follower").build();
        }

        boolean follows = followerRepository.follows(follower, user);

        if(!follows){
            return Response.status(Response.Status.FORBIDDEN).entity("Perdeu mane").build();
        }


        var query = repository.find(  "user",
                Sort.by("dataTime", Sort.Direction.Descending), user);

        var list = query.list();

        var postResponseList = list.stream().map(post -> PostResponse.fromEntity(post))
                .collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }
}
