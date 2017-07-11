/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package jazmin.deploy.domain.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import jazmin.deploy.domain.OutputListener;
import jazmin.log.Logger;
import jazmin.log.LoggerFactory;
import jazmin.util.DumpUtil;
/**
 * 
 * @author yama
 * 25 Dec, 2015
 */
public class WorkingCopy {
	private static Logger logger=LoggerFactory.get(WorkingCopy.class);
	//   
    private  SVNClientManager ourClientManager;
    private  ISVNEventHandler myCommitEventHandler;
    private  ISVNEventHandler myUpdateEventHandler;
    private  ISVNEventHandler myWCEventHandler;
    SVNURL repositoryURL;
    SVNRepository repository;
    File destPath;
    private OutputListener outputListener;
    static{
    	setupLibrary();
    }
    //
    
    //
    public WorkingCopy(String name,String password,String svnPath,String localPath) {
        try {
            repositoryURL = SVNURL.parseURIEncoded(svnPath);
            repository = SVNRepositoryFactory.create(repositoryURL);
            repository.setAuthenticationManager(SVNWCUtil.createDefaultAuthenticationManager(name,password.toCharArray()));
        } catch (SVNException e) {
            //
        }
    	myUpdateEventHandler = new UpdateEventHandler(this);
        myWCEventHandler = new WCEventHandler(this);
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        ourClientManager = SVNClientManager.newInstance(options, name, password);
        ourClientManager.getCommitClient().setEventHandler(myCommitEventHandler);
        ourClientManager.getUpdateClient().setEventHandler(myUpdateEventHandler);
        ourClientManager.getWCClient().setEventHandler(myWCEventHandler);
        //
        if(localPath!=null){
        	destPath=new File(localPath);
        }
    }
    /**
	 * @return the outputListener
	 */
	public OutputListener getOutputListener() {
		return outputListener;
	}
	/**
	 * @param outputListener the outputListener to set
	 */
	public void setOutputListener(OutputListener outputListener) {
		this.outputListener = outputListener;
	}
	//
    public void println(String s){
    	if(outputListener!=null){
    		outputListener.onOutput(s+"\n");
    	}
    	if(logger.isDebugEnabled()){
    		logger.debug(s);
    	}
    }
    //
    /*
     * Initializes the library to work with a repository via 
     * different protocols.
     */
    private static void setupLibrary() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }
    /*
     * 
     */
    public  long checkout()
            throws SVNException {
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doCheckout(repositoryURL, destPath, 
        		SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,true);
    }
    //
    public  void cleanup()
            throws SVNException {
    	try{
    		SVNWCClient wcClient = ourClientManager.getWCClient();
    		wcClient.doCleanup(destPath);
    	}catch(Exception e){}
    }
    /*
     *   
     */
    @SuppressWarnings("deprecation")
	public long update()
            throws SVNException {
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        return updateClient.doUpdate(destPath, SVNRevision.HEAD, true);
    }
    //
    public List<String> logs(long startRevision,long endRevision,int pageSize){
    	List<String> histories=new ArrayList<>();
    	try{
    		repository.log(new String[]{""},startRevision,endRevision,true,true,pageSize,
                    (logEntry)->{
                    	 histories.add(logEntry.getRevision()+"");
                    });
		}catch(Exception e){
			logger.error(e);
		}
    	return histories;
    }
    //
    public void copy(String targetDirUrl,String commit){
    	try {
    		SVNURL repositoryTrgtUrl = SVNURL.parseURIEncoded(targetDirUrl);
        	SVNCopyClient client=ourClientManager.getCopyClient();
        	SVNCopySource[] copySources = new SVNCopySource[1];
            copySources[0] = new SVNCopySource(SVNRevision.HEAD,
            		SVNRevision.HEAD,repositoryURL);
            	
        	client.doCopy(copySources, repositoryTrgtUrl, false, true, false, commit, null);
		} catch (Exception e) {
			logger.error(e);
		}
    }
    //
    public void delete(String sourceDirUrl,String commit){
    	try {
    		SVNURL repositorySrcUrl = SVNURL.parseURIEncoded(sourceDirUrl);
        	SVNCommitClient client=ourClientManager.getCommitClient();
            client.doDelete(new SVNURL[]{repositorySrcUrl},commit);
		} catch (Exception e) {
			logger.error(e);
		}
    }
    //
    public static void main(String[] args) throws Exception {
       WorkingCopy wc=new WorkingCopy(
    		   "svnuser", 
				"svnuser",
    		   "svn://web1.itit.io/repo/CdzDB",
    		   "/tmp/repo");
       wc.cleanup();
       wc.checkout();
       wc.update();
       List<String> list=wc.logs(-1,0,10);
       System.out.println(DumpUtil.dump(list));
    }
}