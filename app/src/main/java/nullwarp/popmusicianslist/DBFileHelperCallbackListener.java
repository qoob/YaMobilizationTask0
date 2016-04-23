package nullwarp.popmusicianslist;

/**
 * Callbacks after DB update
 */
interface DBFileHelperCallbackListener {
    void onDBUpdateSuccess(long prevModified, int status);

    void onDBUpdateFailure(long prevModified, int status);
}
