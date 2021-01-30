/*
You're tasked with building a simple Blog API (no frontend needed!) in Kotlin (or Java) with Spring Boot.
The API should be able to handle some standard tasks, like creating a post, updating a post and finding posts by different filters (see more details below).

You shouldn't spend more than maximum 4 hours on the task.
If you run out of time - think about what you would've done if you had more time and why you'd do those specific things.

Conditions of Satisfaction
-----
- The solution is built in Kotlin or Java using Spring boot
- The solution is runnable from a docker container
- Data is stored in a MySQL or H2 database
- There are some tests (if you don't have time, spend some time to think about how you would've tested the solution and why)
- There is a REST API with JSON input and output that has the following endpoints:
    - Create a new post. The Post should have a title, text content and an optional list of tags.
    - Remove a post
    - Updating a post
    - Fetching all posts
    - Fetching all posts that have a specific tag

Things to send in
-----
- Your code (including the dockerfile), either as a link to a repository or as a zip-file
- A short description of your solution and why you decided to build it like this
- A couple of sentences about how you would handle additional non-functional requirements such as:
    - High load/traffic
    - Authentication & Security
*/

package io.ulrik.blog

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

import javax.persistence.Table
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.GenerationType
import javax.persistence.GeneratedValue
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.ManyToOne
import javax.persistence.JoinTable
import javax.persistence.JoinColumn
import javax.persistence.CascadeType

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import org.springframework.data.repository.CrudRepository

@SpringBootApplication
class BlogApplication

fun main(args: Array<String>) {
	runApplication<BlogApplication>(*args)
}


@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "id")
@Table(name = "BlogPost")
class BlogPost(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,
    var title: String = "",
    var body: String = "",
    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "BlogPost_Tag",
        joinColumns = [JoinColumn(name = "blogpost_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_name")]
    )
    var tags: List<Tag> = listOf()

)

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator::class, property = "name")
@Table(name = "Tag")
class Tag(
    @Id
    var name: String = "",
    @ManyToMany(mappedBy = "tags")
    var posts: List<BlogPost> = listOf()

)

interface BlogRepository: CrudRepository<BlogPost, Long>

interface TagRepository: CrudRepository<Tag, String>


@RestController
class BlogController(val blogRepo: BlogRepository, val tagRepo: TagRepository) {

    @GetMapping
    fun findAll() = blogRepo.findAll()

    @PostMapping
    fun addBlogPost(@RequestBody blogPost: BlogPost)
        = blogRepo.save(blogPost)

    @PutMapping("/{id}")
    fun updateBlogPost(@PathVariable id: Long, @RequestBody blogPost: BlogPost) {
        assert(blogPost.id == id)
        blogRepo.save(blogPost)
    }

    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: Long)
        = blogRepo.deleteById(id)


    @GetMapping("/tag/{tag_name}")
    fun getByTag(@PathVariable tag_name: String)
        = tagRepo.findById(tag_name)
}