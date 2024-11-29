import { useEffect, useRef, useState } from 'react';
import neo4j from 'neo4j-driver';
import * as d3 from 'd3';

function Neo4jDB() {
    const [data, setData] = useState({ nodes: [], links: [] });
    const svgRef = useRef();

    useEffect(() => {
        const driver = neo4j.driver(
            'neo4j+s://9d807b45.databases.neo4j.io',
            neo4j.auth.basic('neo4j', 'm5g_-SKT4P7W6zX2Djgg4qGn9TpUR6RIXYqCCGWFLME')
        );
        const session = driver.session();

        const fetchData = async () => {
            try {
                console.log('Connecting to Neo4j database...');
                const result = await session.run(`
          MATCH (n)-[r]->(m)
          RETURN n, r, m
        `);

                console.log('Data fetched from Neo4j:', result);

                const nodes = [];
                const links = [];

                result.records.forEach(record => {
                    const node1 = record.get('n');
                    const node2 = record.get('m');
                    const relationship = record.get('r');

                    nodes.push({ id: node1.identity.toString(), label: node1.labels[0], properties: node1.properties });
                    nodes.push({ id: node2.identity.toString(), label: node2.labels[0], properties: node2.properties });
                    links.push({ source: node1.identity.toString(), target: node2.identity.toString(), type: relationship.type });
                });

                setData({ nodes, links });
            } catch (error) {
                console.error('Error fetching data from Neo4j:', error);
            } finally {
                await session.close();
                console.log('Session closed.');
            }
        };

        fetchData();

        return () => {
            driver.close();
            console.log('Driver closed.');
        };
    }, []);

    useEffect(() => {
        const svg = d3.select(svgRef.current);
        svg.selectAll('*').remove(); // Clear previous content

        const width = 800;
        const height = 600;

        const simulation = d3.forceSimulation(data.nodes)
            .force('link', d3.forceLink(data.links).id(d => d.id).distance(100))
            .force('charge', d3.forceManyBody().strength(-300))
            .force('center', d3.forceCenter(width / 2, height / 2));

        const link = svg.append('g')
            .attr('stroke', '#999')
            .attr('stroke-opacity', 0.6)
            .selectAll('line')
            .data(data.links)
            .enter().append('line')
            .attr('stroke-width', d => Math.sqrt(d.value));

        const node = svg.append('g')
            .attr('stroke', '#fff')
            .attr('stroke-width', 1.5)
            .selectAll('circle')
            .data(data.nodes)
            .enter().append('circle')
            .attr('r', 5)
            .attr('fill', '#69b3a2')
            .call(d3.drag()
                .on('start', dragstarted)
                .on('drag', dragged)
                .on('end', dragended));

        node.append('title')
            .text(d => d.id);

        simulation.on('tick', () => {
            link
                .attr('x1', d => d.source.x)
                .attr('y1', d => d.source.y)
                .attr('x2', d => d.target.x)
                .attr('y2', d => d.target.y);

            node
                .attr('cx', d => d.x)
                .attr('cy', d => d.y);
        });

        function dragstarted(event, d) {
            if (!event.active) simulation.alphaTarget(0.3).restart();
            d.fx = d.x;
            d.fy = d.y;
        }

        function dragged(event, d) {
            d.fx = event.x;
            d.fy = event.y;
        }

        function dragended(event, d) {
            if (!event.active) simulation.alphaTarget(0);
            d.fx = null;
            d.fy = null;
        }
    }, [data]);

    return (
        <div>
            <h2>Graph Visualization</h2>
            <svg ref={svgRef} width="800" height="600"></svg>
        </div>
    );
}

export default Neo4jDB;