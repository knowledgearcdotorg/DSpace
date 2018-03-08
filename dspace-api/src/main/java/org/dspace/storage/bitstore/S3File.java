/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.storage.bitstore;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import edu.sdsc.grid.io.GeneralFile;
import org.apache.log4j.Logger;
import org.dspace.core.ConfigurationManager;

import java.io.IOException;
import java.io.InputStream;

public class S3File extends GeneralFile{

    private static Logger log = Logger.getLogger(S3File.class);

    public AmazonS3 amazonS3;

    public S3File(AmazonS3 amazonS3) {
        super(null);
        this.amazonS3 = amazonS3;
    }

    public S3File(AmazonS3 amazonS3, String fileName) {
        this(amazonS3);
        this.fileName = fileName;
    }

    public void createS3File(InputStream is) throws IOException {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ConfigurationManager.getProperty("s3.bucket"), fileName, is, new ObjectMetadata());
            putObjectRequest.getRequestClientOptions().setReadLimit(100000);

            amazonS3.putObject(putObjectRequest);
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
            throw new IOException(ase.getCause());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
            throw new IOException(ace.getCause());
        }
    }

    public InputStream retrieveS3File() throws IOException {
        log.warn("Dumping stacktrace when S3 file is retrieved:", new Exception("Exception created for dumping stack trace when S3 file is retrived"));
        try {
            return amazonS3.getObject(new GetObjectRequest(
                    ConfigurationManager.getProperty("s3.bucket"), fileName)).getObjectContent();
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
            throw new IOException(ase.getCause());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
            throw new IOException(ace.getCause());
        }
    }

    @Override
    public boolean delete() {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(
                    ConfigurationManager.getProperty("s3.bucket"), fileName));
            return true;
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }
        return false;
    }

    @Override
    public long length() {
        try {
            return amazonS3.getObjectMetadata(new GetObjectMetadataRequest(
                    ConfigurationManager.getProperty("s3.bucket"), fileName)).getContentLength();
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }
        return 0L;
    }

    @Override
    public boolean exists() {
        try {
            return amazonS3.getObjectMetadata(new GetObjectMetadataRequest(
                    ConfigurationManager.getProperty("s3.bucket"), fileName)).getContentLength() > 0;
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }
        return false;
    }

    @Override
    public long lastModified() {
        try {
            return amazonS3.getObjectMetadata(new GetObjectMetadataRequest(
                    ConfigurationManager.getProperty("s3.bucket"), fileName)).getLastModified().getTime();
        } catch (AmazonServiceException ase) {
            log.error("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            log.error("Error Message:    " + ase.getMessage());
            log.error("HTTP Status Code: " + ase.getStatusCode());
            log.error("AWS Error Code:   " + ase.getErrorCode());
            log.error("Error Type:       " + ase.getErrorType());
            log.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            log.error("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            log.error("Error Message: " + ace.getMessage());
        }
        return 0L;
    }
}
