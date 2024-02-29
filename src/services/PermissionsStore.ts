import { injectable } from 'inversify';
import { ObservableSet, action, makeObservable } from 'mobx';
import { PermissionsAndroid, Permission, PermissionStatus, Rationale } from 'react-native';

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
        }
        if (result === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN) {
            this.blockedPermissions.add(permission);
        }
        return result;
    }

    private async requestMultiplePermissions(permissions: Permission[]) {
        const results = await PermissionsAndroid.requestMultiple(permissions);
        const entries = Object.entries(results) as [Permission, PermissionStatus][];
        return entries.map(([permission, status]) => this.handlePermissionResult(permission, status)).every(s => s === PermissionsAndroid.RESULTS.GRANTED);
    }

    public async requestPermission(permission: Permission, rationale?: Rationale): Promise<PermissionStatus>;
    // eslint-disable-next-line no-dupe-class-members
    public async requestPermission(permission: Permission[]): Promise<boolean>;
    // eslint-disable-next-line no-dupe-class-members
    public async requestPermission(permission: Permission | Permission[], rationale?: Rationale): Promise<PermissionStatus | boolean> {
        if (Array.isArray(permission)) {
            return this.requestMultiplePermissions(permission);
        }
        if (this.grantedPermissions.has(permission)) {
            return PermissionsAndroid.RESULTS.GRANTED;
        }
        const result = await PermissionsAndroid.request(permission, rationale);
        return this.handlePermissionResult(permission, result);
    }

    public async checkPermission(permission: Permission) {
        if (this.grantedPermissions.has(permission)) {
            return true;
        }
        if (this.blockedPermissions.has(permission)) {
            return false;
        }
        const result = await PermissionsAndroid.check(permission);
        if (result) {
            this.grantedPermissions.add(permission);
        }
        if (!result) {
            this.blockedPermissions.add(permission);
        }
        return result;
    }
}
