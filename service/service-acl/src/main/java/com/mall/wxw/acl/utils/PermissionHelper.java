package com.mall.wxw.acl.utils;

import com.mall.wxw.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wxw24633
 * @Time: 2023/10/15  14:23
 */
public class PermissionHelper {


    public static List<Permission> buildPermission(List<Permission> permissionList) {
        //创建最终封装集合
        ArrayList<Permission> trees = new ArrayList<>();
        //遍历所有菜单集合得到第一层数据pid=0
        for (Permission permission : permissionList) {
            if (permission.getPid() == 0){
                permission.setLevel(1);
                //从第一层往下找
                trees.add(findChildren(permission,permissionList));
            }
        }
        return trees;
    }

    //递归往下找
    private static Permission findChildren(Permission permission, List<Permission> permissionList) {
        permission.setChildren(new ArrayList<>());
        for (Permission permission1 : permissionList) {
            if (permission1.getPid().longValue() == permission.getId().longValue()){
                permission1.setLevel(permission.getLevel()+1);
                if (permission.getChildren()==null){
                    permission.setChildren(new ArrayList<>());
                }
                //封装下层数据
                permission.getChildren().add(findChildren(permission1,permissionList));
            }
        }
        return permission;
    }
}
