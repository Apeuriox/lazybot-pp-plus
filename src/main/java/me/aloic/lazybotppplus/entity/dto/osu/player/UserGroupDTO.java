package me.aloic.lazybotppplus.entity.dto.osu.player;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class UserGroupDTO extends GroupDTO implements Serializable{
    private List<String> playmodes;
}
