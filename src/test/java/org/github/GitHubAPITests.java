package org.github;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class GitHubAPITests {


    @BeforeAll
    public static void setup(){

        baseURI="https://api.github.com";
    }

    /**
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Path param org with value cucumber
     * 2. Verify status code 200, content type application/json; charset=utf-8
     * 3. Verify value of the login field is cucumber
     * 4. Verify value of the name field is cucumber
     * 5. Verify value of the id field is 320565
     */

    @Test
    @DisplayName("Verify organization information")
    public void organizationInfoTest(){
        Response response = given().
                                    get("/orgs/{org}","cucumber");

                    response.then().
                                    statusCode(200).
                                    contentType("application/json; charset=utf-8").
                              and().
                                    body("login",is("cucumber")).
                                    body("name", equalToIgnoringCase("cucumber")).
                                    body("id",is(320565));

         // 2. way
        JsonPath json = response.jsonPath();
        String login_cucumber = response.body().path("login");
        String name_Cucumber = response.body().path("name");
        int id = json.getInt("id");


        assertEquals(200,response.statusCode());
        assertEquals("application/json; charset=utf-8",response.contentType());
        assertEquals("cucumber",login_cucumber);
        assertEquals("Cucumber",name_Cucumber);
        assertEquals(320565,id);



    }


    /**
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Header Accept with value application/xml
     * • Path param org with value cucumber
     * 2. Verify status code 415, content type application/json; charset=utf-8
     * 3. Verify response status line include message Unsupported Media Type
     */

    @Test
    @DisplayName("Verify error message")
    public void errorMessageTest(){
        Response response = given().
                                    accept(ContentType.XML).
                                    pathParam("org","cucumber").
                                    get("/orgs/{org}").prettyPeek();
                    response.then().
                                    assertThat().
                                    statusCode(415).
                                    contentType(ContentType.JSON).
                             and().
                                    statusLine(containsString("Unsupported Media Type"));

        // 2. way
        String statusLine = response.statusLine();
        assertEquals(415,response.statusCode());
        assertEquals("application/json; charset=utf-8",response.contentType());
        assertTrue(statusLine.contains("Unsupported Media Type"));
    }

    /**
     * 1. Send a get request to /orgs/:org. Request includes :
     * • Path param org with value cucumber
     * 2. Grab the value of the field public_repos
     * 3. Send a get request to /orgs/:org/repos. Request includes :
     * • Path param org with value cucumber
     * 4. Verify that number of objects in the response is equal to value from step 2
     */
    @Test
    @DisplayName("Number of repositories")
    public void numOfRepoTest(){

        Response response1 = given().
                                    pathParam("org","cucumber").
                             when().
                                    get("/orgs/{org}").prettyPeek();

        int numOfRepositories = response1.body().path("public_repos");


        Response response2 = given().
                                     pathParam("org.","cucumber").
                                     queryParam("per_page",100).
                             when().
                                     get("/orgs/{org.}/repos");
                   response2.then().
                                    body("size()",is(numOfRepositories));


        //Object jSON = response2.body().prettyPeek();

        List<Map<String ,?>> numOfObjects = response2.jsonPath().getList("");
        System.out.println("objects_num.size() = " + numOfObjects.size());
        assertEquals(numOfRepositories,numOfObjects.size());


    }


    /**
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     *      • Path param org with value cucumber
     * 2. Verify that id field is unique in every in every object in the response
     * 3. Verify that node_id field is unique in every in every object in the response
     */
    @Test
    @DisplayName("Repository id information")
    public void repoIdInfo(){
        Response response = given().
                                    pathParam("org.","cucumber").
                            when().
                                    get("/orgs/{org.}/repos").prettyPeek();

        List<Object> id_field = response.body().path("id");
        Set<Object> unique_id = new TreeSet<>();
        unique_id.addAll(id_field);
        List<Object > node_id = response.body().path("node_id");
        Set<Object> unique_node_id = new TreeSet<>();
        unique_node_id.addAll(node_id);

                        response.then().
                                        assertThat().
                                        body("size()",is(unique_id.size())).
                                        body("size()",is(unique_node_id.size()));




        assertTrue(id_field.size()==unique_id.size());
        assertTrue(node_id.size()==unique_node_id.size());




    }

    /**
     *

     1. Send a get request to /orgs/:org. Request includes :
        • Path param org with value cucumber
     2. Grab the value of the field id
     3. Send a get request to /orgs/:org/repos. Request includes :
     • Path param org with value cucumber
     4. Verify that value of the id inside the owner object in every response
        is equal to value from step 2
     */

    @Test
    @DisplayName("Repository owner information")
    public void repoOwnerInfoTest(){
        Response response1 = given().pathParam("org","cucumber").
                                get("/orgs/{org}");

        int id = response1.body().path("id");
        System.out.println("id = " + id);

        Response response2 =
                given().
                        queryParams("per_page",100).
                        pathParam("org","cucumber").
                        when().
                        get("/orgs/{org}/repos").prettyPeek();
        response2.then().
                        assertThat().
                        body("owner.id",everyItem(is(id)));
        // second way
        Set<Integer> IDs = new HashSet<>(response2.jsonPath().get("owner.id"));
        assertEquals(id, IDs.iterator().next());

    }


    /**
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     *  • Path param org with value cucumber
     *  • Query param sort with value full_name
     * 2. Verify that all repositories are listed in alphabetical order
     * based on the value of the field name
     */
    @Test
    @DisplayName("Ascending order by full_name sort")
    public void sortingTest(){
        Response response = given().pathParam("org","cucumber").
                                    queryParam("sort","full_name").
                                    get("/orgs/{org}/repos").prettyPeek();

        List<String> fullNames = response.body().path("full_name");
        List<String > sortedFullNames = new ArrayList<>(fullNames);
        Collections.sort(sortedFullNames);

                            assertEquals(fullNames,sortedFullNames);
    }

    /**
     * 1. Send a get request to /orgs/:org/repos. Request includes :
     *      • Path param org with value cucumber
     *      • Query param sort with value full_name
     *      • Query param direction with value desc
     * 2. Verify that all repositories are listed
     * in reverser alphabetical order based on the value of the field name
     */

    @Test
    @DisplayName("Descending order by full_name sort")
    public void full_nameSortingTest(){
        Response response = given().pathParam("org.","cucumber").
                                 queryParam("sort","full_name").
                                 queryParam("direction","desc").
                                 get("/orgs/{org.}/repos");

        List< String > fullNames = response.body().path("full_name");
        List<String > sortedFullNames = new ArrayList<>(fullNames);
        Collections.sort(sortedFullNames,Collections.reverseOrder());

                        assertEquals(fullNames,sortedFullNames);
    }


    /**
     * 1. Send a get request to /orgs/:org/repos.
     *  Request includes : • Path param org with value cucumber
     * 2. Verify that by default all repositories are listed
     * in descending order based on the value of the field created_at
     */
    @Test
    @DisplayName("Default sort")
    public void defaultSortTest(){

        Response response = given().pathParam("org","cucumber").
                            get("/orgs/{org}/repos");
        
        List< String > create_at = response.body().path("created_at");
        List<String> sorted_create_at = new ArrayList<>(create_at);
        Collections.sort(sorted_create_at);


        assertEquals(create_at,sorted_create_at);


    }







}


















