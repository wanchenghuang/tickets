package com.track.security.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.track.core.exception.LoginFailLimitException;
import com.track.data.bo.user.permission.PermissionBo;
import com.track.data.bo.user.permission.RoleBo;
import com.track.data.bo.user.permission.UserInfoBo;
import com.track.data.domain.po.user.UmUserPo;
import com.track.data.mapper.base.IBaseMapper;
import com.track.data.mapper.permission.SysRolePermissionMapper;
import com.track.data.mapper.permission.SysRoleUserMapper;
import com.track.security.details.authentication.MyWebAuthenticationDetails;
import com.track.security.details.user.SecurityUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author cheng
 * @create 2019-10-17 13:28
 *
 * 实现认证提供者接口，将用户提交的信息加到AuthenticationManagerBuilder中，
 * 由ProviderManager管理获取并生成Authentication
 *
 */
@Component
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private IBaseMapper<UmUserPo> userMapper;

    @Autowired
    private IBaseMapper<SysRoleUserMapper> roleUserMapper;

    @Autowired
    private IBaseMapper<SysRolePermissionMapper> rolePermissionMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * @Author chauncy
     * @Date 2019-10-17 23:59
     * @Description //不指定具体的loginType，则提示未登录
     *
     * 验证传入的authentication信息
     * 验证成功则返回一个包含authorities，并设置isAuthorized=true的完整Authentication
     * 验证失败则抛出异常
     *
     * @Update chauncy
     *
     * @param  authentication
     * @return org.springframework.security.core.Authentication
     **/
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        MyWebAuthenticationDetails details = (MyWebAuthenticationDetails) authentication.getDetails();
        //加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //获取密码并加密
        String encryptPass = bCryptPasswordEncoder.encode(authentication.getCredentials().toString());
        //获取表单中用户名
        String username = authentication.getName();
        //获取密码
        String password = authentication.getCredentials().toString();

        //处理超过登录限制异常
        String flagKey = "loginFailFlag:"+username;
        String value = redisTemplate.opsForValue().get(flagKey);
        Long timeRest = redisTemplate.getExpire(flagKey, TimeUnit.MINUTES);
        System.out.println(timeRest.toString());
        if(StringUtils.isNotBlank(value)){
            //超过限制次数
            throw new LoginFailLimitException("登录错误次数超过限制，请"+timeRest+"分钟后再试");
        }

        switch (details.getLoginType()) {
            //后台用户管理，涉及权限管理
            case MANAGE:
                //通过用户名查找信息，这一步操作可以通过自定义实现UserDetailsService类重写loadUsername(String username)方法,查询用户信息进而判断账号密码异常，
                // 暂时不实现，也没必要了
                //根据用户名查找用户基本信息、绑定角色、权限等
                UserInfoBo userInfo = new UserInfoBo();
                //用户基本信息
                UmUserPo userPo = userMapper.selectOne(new QueryWrapper<UmUserPo>().lambda()
                        .eq(UmUserPo::getUsername,username));
                BeanUtils.copyProperties(userPo,userInfo);

                //绑定角色
                List<RoleBo> roleBos = roleUserMapper.findUserRoleByUserId(userPo.getId());
                //绑定权限
                List<PermissionBo> permissionBos = rolePermissionMapper.findPermissionByUserId(userPo.getId());
                //设置角色和权限，给getAuthorities传参生成Collection<? extends GrantedAuthority>
                userInfo.setRoles(roleBos);
                userInfo.setPermissions(permissionBos);

                //需要处理异常情况，不能使用SecurityUserDetails里面实现的判断方法，因为不走AuthenticationProvider接口的默认实现类AbstractUserDetailsAuthenticationProvider里面的判断了
                //MyAuthenticationProvider抛出异常,需要被SimpleUrlAuthenticationFailureHandler捕捉
                //自定义一个继承了SimpleUrlAuthenticationFailureHandler失败处理器AuthenticationFailHandler
                // 对异常进行处理，默认实现类
                if (userInfo == null) {
                    throw new DisabledException("用户名不存在");
                }else if (!bCryptPasswordEncoder.matches(password, userPo.getPassword())) {
                    throw new BadCredentialsException("密码不正确");
                }else if (userPo.getStatus()==-1){
                    throw new LockedException("账户被禁用，请联系管理员");
                }

                //UserDetails提供用户基本信息，但一般不会用它来存储用户信息，如getUserName(),getPassword(),List<? extends GrantedAuthority>   getAuthorities()等；
                // 而是包装后放到Authentication中的Principle和authorities，使用UsernamePasswordAuthenticationToken()封装
                //用户一般需要自定义一个继承user的并实现UserDetails的SecurityUserDetails类，来满足额外的需求，比如账号过期、禁用等等
                SecurityUserDetails userDetails = new SecurityUserDetails(userInfo);
                //获取给予的授权
                Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                //默认的实现类也会包装,加密的权限会在FilterAuthenticationIntecepter实现类中用到，用于每次请求的添加权限
                return new UsernamePasswordAuthenticationToken(userDetails,encryptPass,authorities);
            case APP_PASSWORD:
                break;
            case APP_CODE:
                break;
            case THIRD_WECHAT:
                break;
        }
        return null;
    }

    /**
     * 判断AuthenticationProvider是否支持authentication的补全
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
