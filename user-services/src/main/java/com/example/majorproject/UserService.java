package com.example.majorproject;

//import org.json.simple.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@Component
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    KafkaTemplate<String, String > kafkaTemplate;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    ObjectMapper objectMapper;

    private final String REDIS_PREFIX_USER="user::";

    public void addUser(UserRequest userRequest) {
        User user=User.builder().userName(userRequest.getUserName())
                .age(userRequest.getAge())
                .email(userRequest.getEmail())
                .name(userRequest.getName()).build();
        userRepository.save(user);
        saveInCache(user);
        //kafka producer
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("userName",user.getUserName());
        kafkaTemplate.send("create_wallet",jsonObject.toString());
    }
    //cache
    public void saveInCache(User user){
        Map map=objectMapper.convertValue(user,Map.class);
        redisTemplate.opsForHash().putAll(REDIS_PREFIX_USER+user.getUserName(), map);
        redisTemplate.expire(REDIS_PREFIX_USER+user.getUserName(), Duration.ofHours(12));
    }

    public User getUser(String userName) throws Exception {
        Map map=redisTemplate.opsForHash().entries(REDIS_PREFIX_USER+userName);

        if(map==null || map.size()==0){
            //miss
            User user=userRepository.findByUserName(userName);
            try {
                if (user==null){
                    throw new UserNotFoundException();
                }
                saveInCache(user);//saving in cache before returning
                return user;
            }catch (Exception e){
                throw new UserNotFoundException();
            }
        }else{
            //hit
            return objectMapper.convertValue(map,User.class);
        }
    }
}
