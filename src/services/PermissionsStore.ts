import { injectable } from 'inversify';
import { ObservableSet, action, makeObservable } from 'mobx';
import { PermissionsAndroid, Permission, PermissionStatus } from 'react-native';

@injectable()
export class PermissionsStore {
    public readonly grantedPermissions = new ObservableSet<Permission>();
    public readonly blockedPermissions = new ObservableSet<Permission>();

    constructor() {
        makeObservable<PermissionsStore, 'handlePermissionResult'>(this, {
            handlePermissionResult: action.bound,
        });
    }

    private handlePermissionResult(permission: Permission, result: PermissionStatus) {
        if (result === PermissionsAndroid.RESULTS.GRANTED) {
            this.grantedPermissions.add(permission);
            return true;
        }
        if (result === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
            this.blockedPermissions.add(permission);
        }
        return false;
    }

    private async requestMultiplePermissions(permissions: Permission[]) {
        const results = await PermissionsAndroid.requestMultiple(permissions);
        const entries = Object.entries(results) as [Permission, PermissionStatus][];
        return entries.map(entry => this.handlePermissionResult(...entry)).every(Boolean);
    }

    public async requestPermission(permission: Permission | Permission[]) {
        if (Array.isArray(permission)) {
            return this.requestMultiplePermissions(permission);
        }
        const result = await PermissionsAndroid.request(permission);
        return this.handlePermissionResult(permission, result);
    }
}
